/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.breathingspaceifstub.controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.syntax.either._
import cats.syntax.option._
import play.api.Logging
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.breathingspaceifstub._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError._
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.{BS_Periods_POST, BS_Periods_PUT}
import uk.gov.hmrc.breathingspaceifstub.model.Failure.HttpErrorCode
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

abstract class AbstractBaseController(cc: ControllerComponents) extends BackendController(cc) with Logging {

  def composeResponse[T](status: Int, body: T)(implicit requestId: RequestId, writes: Writes[T]): Result =
    logAndAddHeaders(Status(status)(Json.toJson(body)))

  val withoutBody: BodyParser[Unit] = parse.when(!_.hasBody, parse.empty, invalidBody)

  def withJsonBody[T](implicit reads: Reads[T]): BodyParser[Response[T]] = BodyParser { request =>
    if (request.hasBody) parseBodyAsJson[T](reads)(request) else missingBody(request)
  }

  def logAndGenErrorResult(failure: Failure)(implicit requestId: RequestId): Result =
    logAndGenErrorItem(failure).value

  def logAndSendErrorResult(failure: Failure)(implicit requestId: RequestId): Future[Result] =
    logAndGenErrorItem(failure).send

  private def logAndGenErrorItem(failure: Failure)(implicit requestId: RequestId): HttpError = {
    val details = failure.detailsToNotShareUpstream.fold("")(details => s" Details: $details")
    logger.error(s"$requestId has error code(${failure.baseError.entryName}).$details")
    HttpError.asErrorItem(requestId.correlationId, failure)
  }

  def logAndGenFailureResult(failure: Failure)(implicit requestId: RequestId): Result =
    logAndGenFailure(failure).value

  def logAndSendFailureResult(failure: Failure)(implicit requestId: RequestId): Future[Result] =
    logAndGenFailure(failure).send

  def withHeaderValidation(
    endpointId: EndpointId
  )(f: RequestId => Future[Result])(implicit request: Request[_]): Future[Result] = {
    val headers: Headers = request.headers
    (for {
      correlationId <- validateCorrelation(headers)
      isUserIdRequired <- validateOriginatorId(headers, endpointId)(correlationId)
      _ <- validateUserId(headers, isUserIdRequired)(correlationId)
    } yield f(RequestId(endpointId))).fold(_.send, identity)
  }

  private def logAndGenFailure(failure: Failure)(implicit requestId: RequestId): HttpError = {
    val bE = failure.baseError
    val code =
      if (bE.isInstanceOf[HttpErrorCode]) httpErrorMap.getOrElse(bE.httpCode, bE.httpCode.toString)
      else bE.entryName

    val details = failure.detailsToNotShareUpstream.fold("")(details => s" Details: $details")

    logger.error(s"$requestId has error code(${code}).$details")
    HttpError(requestId.correlationId, failure)
  }

  private def invalidBody: RequestHeader => Future[Result] =
    implicit request => HttpError(retrieveCorrelationId, Failure(INVALID_BODY)).send

  private def logAndAddHeaders(result: Result)(implicit requestId: RequestId): Result = {
    logger.debug(s"Response to $requestId has status(${result.header.status})")
    result
      .withHeaders(
        HeaderNames.CONTENT_TYPE -> MimeTypes.JSON,
        Header.CorrelationId -> requestId.correlationId.toString
      )
      .as(MimeTypes.JSON)
  }

  private def missingBody[T]: BodyParser[Response[T]] = parse.ignore[Response[T]](Left(Failure(MISSING_BODY)))

  private def parseBodyAsJson[T](implicit reads: Reads[T]): BodyParser[Response[T]] =
    parse.tolerantText.map { text =>
      Either
        .catchNonFatal(Json.parse(text))
        .fold[Response[T]](
          _ => Left(Failure(INVALID_JSON)),
          _.validate[T] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(_) => Left(Failure(INVALID_JSON))
          }
        )
    }

  private def validateCorrelation(headers: Headers): Either[HttpError, String] =
    headers
      .get(Header.CorrelationId)
      .fold[Either[HttpError, String]](
        Left(HttpError(none, Failure(MISSING_HEADER)))
      )(Right(_))

  private def validateOriginatorId(headers: Headers, endpointId: EndpointId)(
    implicit correlationId: String
  ): Either[HttpError, Boolean] =
    headers
      .get(Header.OriginatorId)
      .fold[Either[HttpError, Boolean]](
        Left(HttpError(correlationId.some, Failure(MISSING_HEADER)))
      ) { originatorId =>
        Attended.withNameOption(originatorId.toUpperCase) match {

          case Some(Attended.DA2_BS_ATTENDED) if endpointId == BS_Periods_POST || endpointId == BS_Periods_PUT =>
            Left(HttpError(correlationId.some, Failure(INVALID_HEADER)))

          case Some(Attended.DA2_BS_ATTENDED) => Right(true) // UserId is required
          case Some(Attended.DA2_BS_UNATTENDED) => Right(false) // UserId is not required

          case _ => Left(HttpError(correlationId.some, Failure(INVALID_HEADER)))
        }
      }

  private def validateUserId(headers: Headers, isUserIdRequired: Boolean)(
    implicit correlationId: String
  ): Either[HttpError, Unit] =
    headers.get(Header.UserId) match {
      case Some(_) if isUserIdRequired => Right(unit)
      case Some(_) => Left(HttpError(correlationId.some, Failure(INVALID_HEADER)))
      case None if isUserIdRequired => Left(HttpError(correlationId.some, Failure(MISSING_HEADER)))
      case None => Right(unit)
    }
}
