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

package uk.gov.hmrc.breathingspaceifstub.config

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api._
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.breathingspaceifstub.controller.retrieveCorrelationId
import uk.gov.hmrc.breathingspaceifstub.httpErrorMap
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_ENDPOINT, SERVER_ERROR}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.backend.http.JsonErrorHandler
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

class ErrorHandler @Inject()(
  auditConnector: AuditConnector,
  httpAuditEvent: HttpAuditEvent,
  configuration: Configuration
) extends JsonErrorHandler(auditConnector, httpAuditEvent, configuration)
    with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val correlationId = retrieveCorrelationId(request)
    val endpoint = s"${request.method} ${request.path} has status($statusCode)"
    logger.error(s"${logCorrelationId(correlationId)} $endpoint. Client error was: $message}")
    if (statusCode == NOT_FOUND) HttpError(correlationId, Failure(INVALID_ENDPOINT)).send
    else {
      val payload = Json.obj("failures" -> arrayOfFailures(statusCode, removeCodeDetailIfAny(message)))
      HttpError(correlationId, statusCode, payload).send
    }
  }

  override def onServerError(request: RequestHeader, throwable: Throwable): Future[Result] = {
    val correlationId = retrieveCorrelationId(request)
    val endpoint = s"${request.method} ${request.path}"
    logger.error(s"${logCorrelationId(correlationId)} $endpoint", throwable)
    HttpError(correlationId, Failure(SERVER_ERROR)).send
  }

  private def arrayOfFailures(statusCode: Int, message: String): JsArray =
    Json.arr(
      Json.obj(
        "code" -> httpErrorMap.getOrElse[String](statusCode, "SERVER_ERROR"),
        "reason" -> message
      )
    )

  private def logCorrelationId(correlationId: Option[String]): String =
    s"(Correlation-id: ${correlationId.fold("not-provided")(identity)})"

  private def removeCodeDetailIfAny(message: String): String = {
    val ix = message.indexOf("\n")
    message.substring(0, if (ix == -1) message.length else ix)
  }
}
