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

import play.api.http.Status
import play.api.http.Status.{OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers.{await, contentAsJson, status}
import uk.gov.hmrc.breathingspaceifstub.Header
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.support.{BaseISpec, ControllerBehaviours}

import scala.io.Source

class IndividualDetailsControllerStaticISpec extends BaseISpec with ControllerBehaviours {
  implicit val correlationHeaderValue: CorrelationId = CorrelationId(Some(randomUUIDAsString))

  lazy val wsClient: WSClient = appStaticDataOn.injector.instanceOf[WSClient]
  def makeGetRequest(connectionUrl: String)(implicit correlationId: CorrelationId): WSResponse =
    await(
      wsClient
        .url(connectionUrl)
        .withHttpHeaders(Header.CorrelationId -> correlationId.value.get)
        .get()
    )

  private val filter = {
    val Details = "details(nino,dateOfBirth)"
    val NameList = "nameList(name(firstForename,secondForename,surname,nameType))"
    val AddressList =
      "addressList(address(addressLine1,addressLine2,addressLine3,addressLine4,addressLine5,addressPostcode,countryCode,addressType))"
    val Indicators = "indicators(welshOutputInd)"

    s"$Details,$NameList,$AddressList,$Indicators"
  }

  private val detailsForBreathingSpace = "IndividualDetailsForBS.json"
  private val fullPopulationDetails = "IndividualDetails.json"

  "GET /NINO/:nino" should {
    behave.like(aNinoAsErrorCodeEndpoint(nino => getIndividualDetails(nino, Some(filter), staticDataOn = true)))
    behave.like(acceptsCorrelationId(getIndividualDetails("AS000001A", Some(filter), staticDataOn = true)))
    behave.like(ninoSuffixIgnored(nino => getIndividualDetails(nino, Some(filter), staticDataOn = true)))

    "return 200(OK) with the expected individual details when the Url provides the expected filter" in {
      val nino = "AS000001"
      val response = getIndividualDetails(nino, Some(filter), staticDataOn = true)

      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody(nino, detailsForBreathingSpace))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with a individual details (full population) when the Url does not provide a filter" in {
      val nino = "AS000001"
      val response = getIndividualDetails(nino, None, staticDataOn = true)

      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody(nino, fullPopulationDetails))
      checkCorrelationIDInResponse(response)
    }

    "return 422(UNPROCESSABLE_ENTITY) when the Url provides an unexpected filter" in {
      val nino = "AS000001A"
      val response = getIndividualDetails(nino, Some("details(nino,dateOfBirth,cnrIndicator)"), staticDataOn = true)
      status(response) shouldBe UNPROCESSABLE_ENTITY
      checkCorrelationIDInResponse(response)
    }
  }

  private def getExpectedResponseBody(nino: String, filename: String): JsValue = {
    val in = getClass.getResourceAsStream(s"/data/static/individuals/$filename")

    val s = Source
      .fromInputStream(in)
      .getLines
      .map( // remove pre padding whitespace & post colon whitespace from each line (but not whitespaces from values)
        _.replaceAll("^[ \\t]+", "")
          .replaceAll(":[ \\t]+", ":")
          .replaceFirst("\\$\\{nino}", nino)
      )
      .mkString

    Json.parse(s)
  }

}
