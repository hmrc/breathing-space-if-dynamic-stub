/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.breathingspaceifstub.support

import play.api.http.Status
import play.api.http.Status.*
import play.api.mvc.Result
import play.api.test.Helpers.status
import uk.gov.hmrc.breathingspaceifstub.model.CorrelationId

import scala.concurrent.Future
trait ControllerBehaviours { this: BaseISpec =>

  implicit val correlationHeaderValue: CorrelationId

  def aNinoAsErrorCodeEndpoint(createResponse: String => Future[Result]): Unit = {

    "return 400(BAD_REQUEST) when the Nino 'BS000400B' is sent" in {
      val response = createResponse("BS000400B")
      status(response) shouldBe BAD_REQUEST
      checkCorrelationIDInResponse(response)
    }

    "return 404(NOT_FOUND) when the Nino 'BS000404B' is sent" in {
      val response = createResponse("BS000404B")
      status(response) shouldBe NOT_FOUND
      checkCorrelationIDInResponse(response)
    }

    "return 500(SERVER_ERROR) when the Nino 'BS000500B' is sent" in {
      val response = createResponse("BS000500B")
      status(response) shouldBe INTERNAL_SERVER_ERROR
      checkCorrelationIDInResponse(response)
    }

    "return 500(SERVER_ERROR) when the Nino 'BS0005R0B' is sent" in {
      val response = createResponse("BS0005R0B")
      status(response) shouldBe INTERNAL_SERVER_ERROR
      checkCorrelationIDInResponse(response)
    }

    "return 502(BAD_GATEWAY) when the Nino 'BS000502B' is sent" in {
      val response = createResponse("BS000502B")
      status(response) shouldBe BAD_GATEWAY
      checkCorrelationIDInResponse(response)
    }

    "return 503(SERVICE_UNAVAILABLE) when the Nino 'BS000503B' is sent" in {
      val response = createResponse("BS000503B")
      status(response) shouldBe SERVICE_UNAVAILABLE
      checkCorrelationIDInResponse(response)
    }

    "return 500(SERVER_ERROR) when the Nino specifies a non-existing HTTP status code" in {
      val response = createResponse("BS000700B")
      status(response) shouldBe INTERNAL_SERVER_ERROR
      checkCorrelationIDInResponse(response)
    }
  }

  def acceptsCorrelationId(response: Future[Result], expectedStatus: Int = Status.OK): Unit =
    "return same CorrelationId as sent regardless of header name's letter case" in {
      withClue("Mixed case") {
        status(response) shouldBe expectedStatus
        checkCorrelationIDInResponse(response)
      }

      withClue("Lower case") {
        status(response) shouldBe expectedStatus
        checkCorrelationIDInResponse(response)
      }

      withClue("Upper case") {
        status(response) shouldBe expectedStatus
        checkCorrelationIDInResponse(response)
      }
    }

  def ninoSuffixIgnored(createResponse: String => Future[Result], expectedStatus: Int = Status.OK): Unit =
    "ensure Nino suffix is ignored" in {
      withClue("With suffix") {
        val response = createResponse("AS000001A")
        status(response) shouldBe expectedStatus
        checkCorrelationIDInResponse(response)
      }

      withClue("Without suffix") {
        val response = createResponse("AS000001")
        status(response) shouldBe expectedStatus
        checkCorrelationIDInResponse(response)
      }
    }
}
