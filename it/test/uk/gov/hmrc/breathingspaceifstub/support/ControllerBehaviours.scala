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
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.breathingspaceifstub.Header
import uk.gov.hmrc.breathingspaceifstub.model.CorrelationId
import org.scalatest.wordspec.AnyWordSpec
trait ControllerBehaviours { this: BaseISpec =>

  implicit val correlationHeaderValue: CorrelationId

  def aNinoAsErrorCodeEndpoint(wsResponse: String => WSResponse): Unit = {

    "return 400(BAD_REQUEST) when the Nino 'BS000400B' is sent" in {
      val response = wsResponse("BS000400B")
      response.status                       shouldBe Status.BAD_REQUEST
      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
    }

    "return 404(NOT_FOUND) when the Nino 'BS000404B' is sent" in {
      val response = wsResponse("BS000404B")
      response.status                       shouldBe Status.NOT_FOUND
      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
    }

    "return 500(SERVER_ERROR) when the Nino 'BS000500B' is sent" in {
      val response = wsResponse("BS000500B")
      response.status                       shouldBe Status.INTERNAL_SERVER_ERROR
      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
    }

    "return 500(SERVER_ERROR) when the Nino 'BS0005R0B' is sent" in {
      val response = wsResponse("BS0005R0B")
      response.status                       shouldBe Status.INTERNAL_SERVER_ERROR
      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
    }

    "return 502(BAD_GATEWAY) when the Nino 'BS000502B' is sent" in {
      val response = wsResponse("BS000502B")
      response.status                       shouldBe Status.BAD_GATEWAY
      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
    }

    "return 503(SERVICE_UNAVAILABLE) when the Nino 'BS000503B' is sent" in {
      val response = wsResponse("BS000503B")
      response.status                       shouldBe Status.SERVICE_UNAVAILABLE
      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
    }

    "return 500(SERVER_ERROR) when the Nino specifies a non-existing HTTP status code" in {
      val response = wsResponse("BS000700B")
      response.status                       shouldBe Status.INTERNAL_SERVER_ERROR
      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
    }
  }

  def acceptsCorrelationId(response: WSResponse, expectedStatus: Int = Status.OK): Unit =
    "return same CorrelationId as sent regardless of header name's letter case" in {
      withClue("Mixed case") {
        response.status                       shouldBe expectedStatus
        response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
      }

      withClue("Lower case") {
        response.status                       shouldBe expectedStatus
        response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
      }

      withClue("Upper case") {
        response.status                       shouldBe expectedStatus
        response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
      }
    }

  def ninoSuffixIgnored(wsResponse: String => WSResponse, expectedStatus: Int = Status.OK): Unit =
    "ensure Nino suffix is ignored" in {
      withClue("With suffix") {
        val response = wsResponse("AS000001A")
        response.status                       shouldBe expectedStatus
        response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
      }

      withClue("Without suffix") {
        val response = wsResponse("AS000001")
        response.status                       shouldBe expectedStatus
        response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
      }
    }
}
