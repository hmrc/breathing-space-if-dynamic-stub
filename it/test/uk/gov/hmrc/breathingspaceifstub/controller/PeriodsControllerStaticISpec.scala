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
import play.api.test.Helpers
import play.api.test.Helpers.*
import uk.gov.hmrc.breathingspaceifstub.controller.routes.PeriodsController
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.support.{BaseISpec, ControllerBehaviours}

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID
import scala.io.Source

class PeriodsControllerStaticISpec extends BaseISpec with ControllerBehaviours {
  implicit val correlationHeaderValue: CorrelationId = CorrelationId(Some(randomUUIDAsString))

  "GET /NINO/:nino/periods" should {

    behave.like(aNinoAsErrorCodeEndpoint(nino => getPeriods(nino, staticDataOn = true)))
    behave.like(acceptsCorrelationId(getPeriods("AS000001A", staticDataOn = true)))
    behave.like(ninoSuffixIgnored(nino => getPeriods(nino, staticDataOn = true)))

    "return 200(OK) with a single period (full population) when the Nino 'AS000001A' is sent" in {
      val response = getPeriods("AS000001A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("singleBsPeriodFullPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with a single period (partial population) when the Nino 'AS000002A' is sent" in {
      val response = getPeriods("AS000002A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("singleBsPeriodPartialPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with multiple periods (all full population) when the Nino 'AS000003A' is sent" in {
      val response = getPeriods("AS000003A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("multipleBsPeriodsFullPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with multiple periods (all partial population) when the Nino 'AS000004A' is sent" in {
      val response = getPeriods("AS000004A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("multipleBsPeriodsPartialPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) with multiple periods (mixed population) when the Nino 'AS000005A' is sent" in {
      val response = getPeriods("AS000005A", staticDataOn = true)
      status(response) shouldBe OK
      assert(contentAsJson(response) == getExpectedResponseBody("multipleBsPeriodsMixedPopulation.json"))
      checkCorrelationIDInResponse(response)
    }

    "return 200(OK) when the Nino specified is unknown " in {
      withClue("MA000700A") {
        val response = getPeriods("MA000700A", staticDataOn = true)
        status(response) shouldBe OK
        assert(contentAsString(response) == """{"periods":[]}""")
        checkCorrelationIDInResponse(response)
      }

      withClue("MA000200B") {
        val response = getPeriods("MA000200A", staticDataOn = true)
        status(response) shouldBe OK
        assert(contentAsString(response) == """{"periods":[]}""")
        checkCorrelationIDInResponse(response)
      }

      withClue("AB000500D") {
        val response = getPeriods("AB000500D", staticDataOn = true)
        status(response) shouldBe OK
        assert(contentAsString(response) == """{"periods":[]}""")
        checkCorrelationIDInResponse(response)
      }
    }
  }

  "POST /NINO/:nino/periods" should {
    val period1 = PostPeriodInRequest(
      LocalDate.of(2020, 6, 25),
      None,
      LocalDateTime.of(2020, 12, 22, 14, 19).format(timestampFormatter)
    )
    val period2 = PostPeriodInRequest(
      LocalDate.of(2020, 6, 22),
      Some(LocalDate.of(2020, 8, 22)),
      LocalDateTime.of(2020, 12, 22, 14, 19).format(timestampFormatter)
    )
    val periods = List(period1, period2)
    behave.like(
      aNinoAsErrorCodeEndpoint(nino => postPeriods(nino = nino, postPeriods = periods, staticDataOn = true))
    )
    behave.like(
      acceptsCorrelationId(postPeriods(nino = "AS000001A", postPeriods = periods, staticDataOn = true), CREATED)
    )
    behave.like(
      ninoSuffixIgnored(nino => postPeriods(nino = nino, postPeriods = periods, staticDataOn = true), CREATED)
    )

    "return 201(CREATED) with the periods sent when any accepted Nino value is sent" in {
      val response = postPeriods(nino = "AS000400A", postPeriods = periods, staticDataOn = true)
      status(response) shouldBe CREATED
      checkCorrelationIDInResponse(response)
    }

    "return 400(BAD_REQUEST) when the request is sent without json body" in {
      val response = postPeriods(nino = "BS000400A", postPeriods = periods, staticDataOn = true)
      status(response) shouldBe BAD_REQUEST
      checkCorrelationIDInResponse(response)
    }

    "return 400(BAD_REQUEST) when the request is sent with good Nino but without json body" in {
      val response =
        route(appStaticDataOn, unattendedFakeRequest(Helpers.POST, PeriodsController.post("AS000400A").url)).get
      status(response) shouldBe BAD_REQUEST
      checkCorrelationIDInResponse(response)
    }

    "return 400(BAD_REQUEST) when the request is sent with invalid json body" in {
      val response = route(
        appStaticDataOn,
        unattendedFakeRequest(Helpers.POST, PeriodsController.post("AS000400A").url)
          .withJsonBody(Json.obj("notWhatWeAreExpecting" -> "certainlyNot"))
      ).get
      status(response) shouldBe BAD_REQUEST
      checkCorrelationIDInResponse(response)
    }
  }

  "PUT /NINO/:nino/periods" should {
    val period1 = PutPeriodInRequest(
      UUID.randomUUID(),
      LocalDate.of(2020, 6, 25),
      None,
      LocalDateTime.of(2020, 12, 22, 14, 19).format(timestampFormatter)
    )
    val period2 = PutPeriodInRequest(
      UUID.randomUUID(),
      LocalDate.of(2020, 6, 22),
      Some(LocalDate.of(2020, 8, 22)),
      LocalDateTime.of(2020, 12, 22, 14, 19).format(timestampFormatter)
    )
    val periods = List(period1, period2)
    behave.like(
      aNinoAsErrorCodeEndpoint(nino => putPeriods(nino = nino, putPeriods = periods, staticDataOn = true))
    )
    behave.like(
      acceptsCorrelationId(putPeriods(nino = "AS000001A", putPeriods = periods, staticDataOn = true), CREATED)
    )
    behave.like(
      ninoSuffixIgnored(nino => putPeriods(nino = nino, putPeriods = periods, staticDataOn = true), CREATED)
    )

    "return 201(CREATED) with the periods sent when any accepted Nino value is sent" in {
      val response = putPeriods(nino = "AS000400A", putPeriods = periods, staticDataOn = true)
      status(response) shouldBe CREATED
      checkCorrelationIDInResponse(response)
    }

    "return 400(BAD_REQUEST) when the request is sent without json body" in {
      val response = putPeriods(nino = "BS000400A", putPeriods = periods, staticDataOn = true)
      status(response) shouldBe BAD_REQUEST
      checkCorrelationIDInResponse(response)
    }

    "return 400(BAD_REQUEST) when the request is sent with good Nino but without json body" in {
      val response =
        route(appStaticDataOn, unattendedFakeRequest(Helpers.POST, PeriodsController.post("AS000400A").url)).get
      status(response) shouldBe BAD_REQUEST
      checkCorrelationIDInResponse(response)
    }

    "return 400(BAD_REQUEST) when the request is sent with invalid json body" in {
      val response = route(
        appStaticDataOn,
        unattendedFakeRequest(Helpers.POST, PeriodsController.post("AS000400A").url)
          .withJsonBody(Json.obj("notWhatWeAreExpecting" -> "certainlyNot"))
      ).get
      status(response) shouldBe BAD_REQUEST
      checkCorrelationIDInResponse(response)
    }
  }

  private def getExpectedResponseBody(filename: String): JsValue = {
    val in = getClass.getResourceAsStream(s"/data/static/periods/$filename")
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
