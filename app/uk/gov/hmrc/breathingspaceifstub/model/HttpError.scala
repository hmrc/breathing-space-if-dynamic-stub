/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.breathingspaceifstub.model

import scala.concurrent.Future

import cats.syntax.option._
import play.api.Logging
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.Status
import uk.gov.hmrc.breathingspaceifstub.Header

case class HttpError(value: Result) {
  lazy val send = Future.successful(value)
}

object HttpError extends Logging {

  def apply(correlationId: => Option[String], failure: Failure, httpCode: Int = 0): HttpError = {
    val payload = Json.obj("failures" -> List(failure))
    apply(correlationId.toString.some, if (httpCode == 0) failure.baseError.httpCode else httpCode, payload)
  }

  def asErrorItem(correlationId: => Option[String], failure: Failure, httpCode: Int = 0): HttpError = {
    implicit val writes = Failure.asErrorItem
    val payload = Json.obj("errors" -> List(failure))
    apply(correlationId.toString.some, if (httpCode == 0) failure.baseError.httpCode else httpCode, payload)
  }

  def apply(correlationId: Option[String], httpCode: Int, payload: JsObject): HttpError = {
    val headers = List(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)
    new HttpError(
      Status(httpCode)(payload)
        .withHeaders(
          correlationId.fold(headers) { corrId =>
            headers :+ (Header.CorrelationId -> corrId)
          }: _*
        )
    )
  }
}
