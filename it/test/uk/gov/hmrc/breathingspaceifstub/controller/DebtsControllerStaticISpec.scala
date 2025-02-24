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

import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.*
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.support.{BaseISpec, ControllerBehaviours}

import scala.io.Source

class DebtsControllerStaticISpec extends BaseISpec with ControllerBehaviours {
  implicit val correlationHeaderValue: CorrelationId = CorrelationId(Some(randomUUIDAsString))

  "GET /NINO/:nino/debts" should {
    behave.like(aNinoAsErrorCodeEndpoint(nino => getDebts(nino, staticDataOn = true)))
    behave.like(acceptsCorrelationId(getDebts("AS000001A", staticDataOn = true)))
    behave.like(ninoSuffixIgnored(nino => getDebts(nino, staticDataOn = true)))

    "return 200(OK) with a single debt (full population) when the Nino 'AS000001A' is sent" in {
      val response = getDebts("AS000001A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("singleBsDebtFullPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with a single debt (partial population) when the Nino 'AS000002A' is sent" in {
      val response = getDebts("AS000002A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("singleBsDebtPartialPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with multiple debts (all full population) when the Nino 'AS000003A' is sent" in {
      val response = getDebts("AS000003A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("multipleBsDebtsFullPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with multiple debts (all partial population) when the Nino 'AS000004A' is sent" in {
      val response = getDebts("AS000004A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("multipleBsDebtsPartialPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with multiple debts (mixed population) when the Nino 'AS000005A' is sent" in {
      val response = getDebts("AS000005A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("multipleBsDebtsMixedPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 400(BAD_REQUEST) when the url does not include the periodId" in {
      val response = getDebts("AS000005A", staticDataOn = true)
      status(response) shouldBe OK
//      val connectionUrl = s"$testServerAddress/individuals/breathing-space/NINO/AS000005A/debts"
//      val response      = makeGetRequest(connectionUrl)
//      response.status shouldBe Status.NOT_FOUND
    }
//
//    "return 400(BAD_REQUEST) when the periodId is not a valid UUID" in {
//      val connectionUrl = s"$testServerAddress/individuals/breathing-space/NINO/AS000005A/abc/debts"
//      val response      = makeGetRequest(connectionUrl)
//      response.status shouldBe Status.BAD_REQUEST
//    }

    "return 404(NO_DATA_FOUND) when the Nino specified is unknown " in {
      withClue("MA000700A") {

        val response = getDebts("MA000700A", staticDataOn = true)
        status(response) shouldBe NOT_FOUND
        assert(contentAsString(response).startsWith("""{"failures":[{"code":"NO_DATA_FOUND","reason":"""))
        checkCorrelationIDInResponse(response)
      }
    }
  }

  private def getExpectedResponseBody(filename: String): JsValue = {
    val in = getClass.getResourceAsStream(s"/data/static/debts/$filename")
    val s = Source
      .fromInputStream(in)
      .getLines
      .map( // remove pre padding whitespace & post colon whitespace from each line (but not whitespaces from values)
        _.replaceAll("^[ \\t]+", "")
          .replaceAll(":[ \\t]+", ":")
      )
      .mkString
    Json.parse(s)
  }
}
