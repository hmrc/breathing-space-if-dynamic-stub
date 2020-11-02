/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.Logging
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.breathingspaceifstub._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

abstract class AbstractBaseController(cc: ControllerComponents) extends BackendController(cc) with Logging {

  val withoutBody: BodyParser[Unit] = parse.when(!_.hasBody, parse.empty, invalidBody)

  def withJsonBody[T](implicit reads: Reads[T]): BodyParser[Response[T]] = BodyParser { request =>
    if (request.hasBody) parseBodyAsJson[T](reads)(request) else missingBody(request)
  }

  private def invalidBody: RequestHeader => Future[Result] =
    implicit request => HttpError(retrieveCorrelationId, Failure(INVALID_BODY)).send

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

  def composeResponse[T](status: Int, body: T)(implicit requestId: RequestId, writes: Writes[T]): Result =
    logAndAddHeaders(Status(status)(Json.toJson(body)))

  private def logAndAddHeaders(result: Result)(implicit requestId: RequestId): Result = {
    logger.debug(s"Response to $requestId has status(${result.header.status})")
    result
      .withHeaders(
        HeaderNames.CONTENT_TYPE -> MimeTypes.JSON,
        Header.CorrelationId -> requestId.correlationId.toString
      )
      .as(MimeTypes.JSON)
  }
}
