/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.syntax.either.*
import cats.syntax.option.*
import play.api.Logging
import play.api.http.HeaderNames
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.breathingspaceifstub.*
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.*
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.{BS_Memorandum_GET, BS_Periods_POST, BS_Periods_PUT}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

abstract class AbstractBaseController(cc: ControllerComponents, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

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
    logger.error(s"$requestId has error code(${failure.baseError.getClass.getSimpleName}).$details")
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

  protected def getDataFromFile(filename: String): String = {
    val in = getClass.getResourceAsStream(s"/data/$filename")
    Source.fromInputStream(in).getLines().mkString
  }

  protected def sendResponse(httpCode: Int, body: JsValue)(implicit request: Request[_]): Result =
    Status(httpCode)(body)
      .withHeaders(
        Header.CorrelationId -> request.headers
          .get(Header.CorrelationId)
          .getOrElse(UUID.randomUUID().toString)
      )
      .as(play.mvc.Http.MimeTypes.JSON)

  protected def sendResponseBla(nino: String, details: String)(implicit request: Request[_]): Result =
    sendResponse(OK, Json.parse(details.replaceFirst("\\$\\{nino}", nino)))

  protected def failures(code: String, reason: String = "A generic error"): JsValue =
    Json.parse(s"""{"failures":[{"code":"$code","reason":"$reason"}]}""")

  protected def createResult(httpCode: Int, body: JsValue)(implicit request: Request[_]): Result =
    Status(httpCode)(body)
      .withHeaders(
        Header.CorrelationId -> request.headers
          .get(Header.CorrelationId)
          .getOrElse(UUID.randomUUID().toString)
      )
      .as(play.mvc.Http.MimeTypes.JSON)

  protected def withStaticCheck(nino: String)(staticRetrieval: String => Option[Result])(
    f: Request[_] => Future[Result]
  )(implicit request: Request[_]): Future[Result] =
    if (appConfig.isEnabledStaticData) {
      staticRetrieval(nino) match {
        case Some(result) =>
          Future.successful(result)
        case _ =>
          f(request)
      }
    } else {
      f(request)
    }

  private def logAndGenFailure(failure: Failure)(implicit requestId: RequestId): HttpError = {
    val bE = failure.baseError
    val code =
      if (bE.isInstanceOf[HttpErrorCode]) httpErrorMap.getOrElse(bE.httpCode, bE.httpCode.toString)
      else bE.getClass.getSimpleName

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
        HeaderNames.CONTENT_TYPE -> play.mvc.Http.MimeTypes.JSON,
        Header.CorrelationId -> requestId.correlationId.toString
      )
      .as(play.mvc.Http.MimeTypes.JSON)
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

  private def validateOriginatorId(headers: Headers, endpointId: EndpointId)(implicit
    correlationId: String
  ): Either[HttpError, Boolean] =
    headers
      .get(Header.OriginatorId)
      .fold[Either[HttpError, Boolean]](
        Left(HttpError(correlationId.some, Failure(MISSING_HEADER)))
      ) { originatorId =>
        Attended.withNameOption(originatorId.toUpperCase) match {

          case Some(Attended.DA2_BS_ATTENDED) if endpointId == BS_Periods_POST || endpointId == BS_Periods_PUT =>
            Left(HttpError(correlationId.some, Failure(INVALID_HEADER)))

          case Some(Attended.DA2_PTA) if endpointId == BS_Memorandum_GET =>
            Right(false)

          case Some(Attended.DA2_BS_ATTENDED) => Right(true) // UserId is required
          case Some(Attended.DA2_BS_UNATTENDED) => Right(false) // UserId is not required

          case _ => Left(HttpError(correlationId.some, Failure(INVALID_HEADER)))
        }
      }

  private def validateUserId(headers: Headers, isUserIdRequired: Boolean)(implicit
    correlationId: String
  ): Either[HttpError, Unit] =
    headers.get(Header.UserId) match {
      case Some(_) if isUserIdRequired => Right(unit)
      case Some(_) => Left(HttpError(correlationId.some, Failure(INVALID_HEADER)))
      case None if isUserIdRequired => Left(HttpError(correlationId.some, Failure(MISSING_HEADER)))
      case None => Right(unit)
    }
}
