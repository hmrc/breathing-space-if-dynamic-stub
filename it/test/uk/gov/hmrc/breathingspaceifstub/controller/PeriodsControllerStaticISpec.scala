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

import java.time.{LocalDate, LocalDateTime}
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
//    behave.like(acceptsCorrelationId(getPeriods("AS000001A", staticDataOn = true)))
//    behave.like(ninoSuffixIgnored(nino => getPeriods(nino, staticDataOn = true)))

//    behave.like(aNinoAsErrorCodeEndpoint(s => makePostRequest(getConnectionUrl(s), bodyContents)))
//    behave.like(acceptsCorrelationId(makePostRequest(getConnectionUrl("AS000001A"), bodyContents), Status.CREATED))
//    behave.like(ninoSuffixIgnored(s => makePostRequest(getConnectionUrl(s), bodyContents), Status.CREATED))

//    "return 201(CREATED) with the periods sent when any accepted Nino value is sent" in {
//      val response = makePostRequest(getConnectionUrl("AS000400A"), bodyContents)
//      response.status                       shouldBe Status.CREATED
//      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
//    }
//
//    "return 400(BAD_REQUEST) when the request is sent without json body" in {
//      val response = makePutRequest(getConnectionUrl("BS000400A"), bodyContents = "")
//      response.status                       shouldBe Status.BAD_REQUEST
//      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
//    }
//
//    "return 400(BAD_REQUEST) when the request is sent with good Nino but without json body" in {
//      val response = makePutRequest(getConnectionUrl("AS000400A"), bodyContents = "")
//      response.status                       shouldBe Status.BAD_REQUEST
//      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
//    }
//
//    "return 400(BAD_REQUEST) when the request is sent with invalid json body" in {
//      val response = makePostRequest(getConnectionUrl("AS000400A"), """{"notWhatWeAreExpecting":"certainlyNot"}""")
//      response.status                       shouldBe Status.BAD_REQUEST
//      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
//    }
  }
//
//  "PUT /NINO/:nino/periods" should {
//
//    val periodId1    = """"periodID": "4043d4b5-1f2a-4d10-8878-ef1ce9d97b32""""
//    val periodId2    = """"periodID": "6aed4f02-f652-4bef-af14-49c79e968c2e""""
//    val period1      = s"""{$periodId1, "startDate":"2020-06-25","pegaRequestTimestamp":"2020-12-22T14:19:03+01:00"}"""
//    val period2      =
//      s"""{$periodId2, "startDate":"2020-06-22","endDate":"2020-08-22","pegaRequestTimestamp":"2020-12-22T14:19:03+01:00"}"""
//    val bodyContents = s"""{"periods":[$period1,$period2]}"""
//
//    behave.like(aNinoAsErrorCodeEndpoint(s => makePutRequest(getConnectionUrl(s), bodyContents)))
//    behave.like(acceptsCorrelationId(makePutRequest(getConnectionUrl("AS000001A"), bodyContents)))
//    behave.like(ninoSuffixIgnored(s => makePutRequest(getConnectionUrl(s), bodyContents)))
//
//    "return 200(OK) with the periods sent when any accepted Nino value is sent" in {
//      val response = makePutRequest(getConnectionUrl("AS000400A"), bodyContents)
//      response.status                       shouldBe Status.OK
//      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
//    }
//
//    "return 400(BAD_REQUEST) when the request is sent without json body" in {
//      val response = makePutRequest(getConnectionUrl("AS000001A"), bodyContents = "")
//      response.status                       shouldBe Status.BAD_REQUEST
//      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
//    }
//
//    "return 400(BAD_REQUEST) when the request is sent with invalid json body" in {
//      val response = makePutRequest(
//        getConnectionUrl("AS000001A"),
//        """{"notWhatWeAreExpecting":"certainlyNot"}"""
//      )
//      response.status                       shouldBe Status.BAD_REQUEST
//      response.header(Header.CorrelationId) shouldBe correlationHeaderValue.value
//    }
//  }

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
