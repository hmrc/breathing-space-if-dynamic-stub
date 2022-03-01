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

package uk.gov.hmrc.breathingspaceifstub.controller

import java.time.LocalDate
import java.util.UUID

import cats.syntax.option._
import org.scalatest.Assertion
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.controller.routes.PeriodsController
import uk.gov.hmrc.breathingspaceifstub.Header
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_HEADER, INVALID_JSON, MISSING_HEADER}
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class PeriodsControllerISpec extends BaseISpec {

  test("\"get\" Periods should return all periods for an existing Nino, if any") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val postPeriod1 = genPostPeriodInRequest(withEndDate)
    val postPeriod2 = genPostPeriodInRequest()
    status(postPeriods(individual.nino, List(postPeriod1, postPeriod2))) shouldBe CREATED

    val response = getPeriods(individual.nino)
    status(response) shouldBe OK

    val periods = contentAsJson(response).as[Periods].periods
    periods.size shouldBe 2

    periods.head.startDate shouldBe postPeriod1.startDate
    periods.head.endDate shouldBe postPeriod1.endDate

    periods.last.startDate shouldBe postPeriod2.startDate
    periods.last.endDate shouldBe postPeriod2.endDate
  }

  test("\"get\" Periods should return an empty list if no one is found for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val response = getPeriods(individual.nino)
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"periods":[]}"""
  }

  test("\"get\" Periods should report if the CorrelationId header is missing") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val request = FakeRequest(Helpers.GET, PeriodsController.get(individual.nino).url)
      .withHeaders(Header.OriginatorId -> Attended.DA2_BS_UNATTENDED.toString)

    val response = route(app, request).get
    status(response) shouldBe BAD_REQUEST
    assert(contentAsString(response).startsWith(s"""{"failures":[{"code":"${MISSING_HEADER.entryName}"""))
  }

  test("An unattended \"get\" Periods should report if it includes a 'UserId' header") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val headers = unattendedRequestHeaders :+ ((Header.UserId, "0000000"))
    val request = FakeRequest(Helpers.GET, PeriodsController.get(individual.nino).url)
      .withHeaders(headers: _*)

    val response = route(app, request).get
    status(response) shouldBe BAD_REQUEST
    assert(contentAsString(response).startsWith(s"""{"failures":[{"code":"${INVALID_HEADER.entryName}"""))
  }

  test("\"get\" Periods should report if the provided Nino is unknown") {
    status(getPeriods(genNino)) shouldBe NOT_FOUND
  }

  test("\"post\" Periods should successfully add a single period for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val postPeriodsInRequest = List(genPostPeriodInRequest(withEndDate))
    status(postPeriods(individual.nino, postPeriodsInRequest)) shouldBe CREATED
  }

  test("\"post\" Periods should successfully add multiple periods for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val postPeriodsInRequest = List(
      genPostPeriodInRequest(withEndDate),
      genPostPeriodInRequest()
    )
    status(postPeriods(individual.nino, postPeriodsInRequest)) shouldBe CREATED
  }

  test("Multiple \"post\" Periods ops to the same Nino should append all provided periods") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val postPeriodsInRequest1 = List(genPostPeriodInRequest())
    status(postPeriods(individual.nino, postPeriodsInRequest1)) shouldBe CREATED

    val postPeriodsInRequest2 = List(genPostPeriodInRequest(withEndDate), genPostPeriodInRequest())
    status(postPeriods(individual.nino, postPeriodsInRequest2)) shouldBe CREATED

    val response = getPeriods(individual.nino)
    status(response) shouldBe OK

    val periods = contentAsJson(response).as[Periods].periods
    periods.size shouldBe 3
  }

  test("An attended \"post\" Periods should return 400(INVALID_HEADER)") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val request = FakeRequest(Helpers.POST, PeriodsController.post(individual.nino).url)
      .withHeaders(attendedRequestHeaders: _*)

    val response = route(app, request).get
    status(response) shouldBe BAD_REQUEST
    assert(contentAsString(response).startsWith(s"""{"failures":[{"code":"${INVALID_HEADER.entryName}"""))
  }

  test("\"post\" Periods should report duplicated submission") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val consumerRequestId = UUID.randomUUID

    val postPeriodsInRequest1 = List(genPostPeriodInRequest(withEndDate))
    status(postPeriods(individual.nino, consumerRequestId, postPeriodsInRequest1)) shouldBe CREATED

    val postPeriodsInRequest2 = List(genPostPeriodInRequest(withEndDate))
    status(postPeriods(individual.nino, consumerRequestId, postPeriodsInRequest2)) shouldBe CONFLICT
  }

  test("\"post\" Periods should report if the provided Nino is unknown") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val postPeriodsInRequest = List(genPostPeriodInRequest(withEndDate))
    status(postPeriods(genNino, postPeriodsInRequest)) shouldBe NOT_FOUND
  }

  test("\"post\" should return 404(INVALID_JSON) when the list of periods to add is empty") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val postPeriodsInRequest = List.empty
    val response = postPeriods(genNino, postPeriodsInRequest)
    status(response) shouldBe BAD_REQUEST
    assert(contentAsString(response).startsWith(s"""{"failures":[{"code":"${INVALID_JSON.entryName}"""))
  }

  test("\"put\" Periods should successfully update a single period for the provided Nino") {
    val individual = genIndividualInRequest(withPeriods = true)
    status(postIndividual(individual)) shouldBe CREATED

    val getResponse = getPeriods(individual.nino)
    status(getResponse) shouldBe OK
    val periodsFromGet = contentAsJson(getResponse).as[Periods].periods

    val putPeriodInRequest = copyPutPeriodInRequest(periodsFromGet.head)
    val putResponse = putPeriods(individual.nino, List(putPeriodInRequest))
    status(putResponse) shouldBe OK
    val periodsFromPut = contentAsJson(putResponse).as[Periods].periods
    periodsFromPut.size shouldBe periodsFromGet.size

    assertPeriodFromPut(periodsFromPut, putPeriodInRequest)
  }

  test("\"put\" Periods should successfully update multiple periods for the provided Nino") {
    val individual = genIndividualInRequest(individualDetails.some, withPeriods = true)
    status(postIndividual(individual)) shouldBe CREATED

    val postPeriodsInRequest = List(genPostPeriodInRequest())
    status(postPeriods(individual.nino, postPeriodsInRequest)) shouldBe CREATED

    val getResponse = getPeriods(individual.nino)
    status(getResponse) shouldBe OK
    val periodsFromGet = contentAsJson(getResponse).as[Periods].periods

    val putPeriod1InRequest = copyPutPeriodInRequest(periodsFromGet.head)
    val putPeriod2InRequest = copyPutPeriodInRequest(periodsFromGet.tail.head)
    val putResponse = putPeriods(individual.nino, List(putPeriod1InRequest, putPeriod2InRequest))
    status(putResponse) shouldBe OK
    val periodsFromPut = contentAsJson(putResponse).as[Periods].periods
    periodsFromPut.size shouldBe periodsFromGet.size

    assertPeriodFromPut(periodsFromPut, putPeriod1InRequest)
    assertPeriodFromPut(periodsFromPut, putPeriod2InRequest)

    val getResponseAfterPut = getPeriods(individual.nino)
    status(getResponseAfterPut) shouldBe OK
    contentAsJson(getResponseAfterPut).as[Periods].periods.size shouldBe periodsFromGet.size
  }

  test("\"put\" Periods should report if the provided Nino is unknown") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val putPeriodsInRequest = List(genPutPeriodInRequest())
    status(putPeriods(genNino, putPeriodsInRequest)) shouldBe NOT_FOUND
  }

  test("An attended \"put\" Periods should return 400(INVALID_HEADER)") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val request = FakeRequest(Helpers.PUT, PeriodsController.put(individual.nino).url)
      .withHeaders(attendedRequestHeaders: _*)

    val response = route(app, request).get
    status(response) shouldBe BAD_REQUEST
    assert(contentAsString(response).startsWith(s"""{"failures":[{"code":"${INVALID_HEADER.entryName}"""))
  }

  test("\"put\" should return 404(INVALID_JSON) when the list of periods to update is empty") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val putPeriodsInRequest = List.empty
    val response = putPeriods(genNino, putPeriodsInRequest)
    status(response) shouldBe BAD_REQUEST
    assert(contentAsString(response).startsWith(s"""{"failures":[{"code":"${INVALID_JSON.entryName}"""))
  }

  test("\"delete\" should successfully remove a period and its underpayments for the provided nino") {
    val individual = genIndividualInRequest(withPeriods = true)
    status(postIndividual(individual)) shouldBe CREATED

    val getResponse = getPeriods(individual.nino)
    status(getResponse) shouldBe OK

    val periodsFromGet = contentAsJson(getResponse).as[Periods].periods

    val nino = individual.nino
    val period1Id = periodsFromGet.head.periodID
    val underpayments = Underpayments(List(u1, u2, u3))

    status(postUnderpayments(nino, period1Id.toString, underpayments)) shouldBe OK

    val request = FakeRequest(Helpers.DELETE, PeriodsController.delete(nino, period1Id).url)
      .withHeaders(attendedRequestHeaders: _*)

    val response = route(app, request).get
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"periodsDeleted":1,"underpaymentsDeleted":3}""".stripMargin

  }

  test("\"delete\" should successfully remove a period with no underpayments") {
    val individual = genIndividualInRequest(withPeriods = true)
    status(postIndividual(individual)) shouldBe CREATED

    val getResponse = getPeriods(individual.nino)
    status(getResponse) shouldBe OK

    val periodsFromGet = contentAsJson(getResponse).as[Periods].periods

    val nino = individual.nino
    val period1Id = periodsFromGet.head.periodID

    val request = FakeRequest(Helpers.DELETE, PeriodsController.delete(nino, period1Id).url)
      .withHeaders(attendedRequestHeaders: _*)

    val response = route(app, request).get
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"periodsDeleted":1,"underpaymentsDeleted":0}""".stripMargin
  }

  test("\"delete\" should not remove any period if the periodId does not belong to a given nino") {
    val individual = genIndividualInRequest(withPeriods = true)
    status(postIndividual(individual)) shouldBe CREATED
    val nino = individual.nino

    val individual2 = genIndividualInRequest(withPeriods = true)
    status(postIndividual(individual2)) shouldBe CREATED

    val getResponse = getPeriods(individual2.nino)
    status(getResponse) shouldBe OK
    val periodsFromGet = contentAsJson(getResponse).as[Periods].periods
    val periodSecondIndividual = periodsFromGet.head.periodID


    val request = FakeRequest(Helpers.DELETE, PeriodsController.delete(nino, periodSecondIndividual).url)
      .withHeaders(attendedRequestHeaders: _*)

    val response = route(app, request).get
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"periodsDeleted":0,"underpaymentsDeleted":0}""".stripMargin
  }

  test("\"delete\" should not remove any period if the periodId does not exists") {
    val individual = genIndividualInRequest(withPeriods = true)
    status(postIndividual(individual)) shouldBe CREATED
    val nino = individual.nino

    val randomPeriod = UUID.randomUUID()

    val request = FakeRequest(Helpers.DELETE, PeriodsController.delete(nino, randomPeriod).url)
      .withHeaders(attendedRequestHeaders: _*)

    val response = route(app, request).get
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"periodsDeleted":0,"underpaymentsDeleted":0}""".stripMargin
  }

  test("\"delete\" should not remove any period if the periodId or underpayment if the nino does not exists ") {
    val randomNino = genNino
    val randomPeriod = UUID.randomUUID()

    val request = FakeRequest(Helpers.DELETE, PeriodsController.delete(randomNino, randomPeriod).url)
      .withHeaders(attendedRequestHeaders: _*)

    val response = route(app, request).get
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"periodsDeleted":0,"underpaymentsDeleted":0}""".stripMargin
  }

  private def copyPutPeriodInRequest(period: Period): PutPeriodInRequest =
    genPutPeriodInRequest().copy(
      periodID = period.periodID,
      startDate = period.startDate.plusMonths(1L),
      endDate = LocalDate.now.plusYears(1).some
    )

  private def assertPeriodFromPut(periodsFromPut: List[Period], period: PutPeriodInRequest): Assertion = {
    val periodFromPut = periodsFromPut.filter(_.periodID == period.periodID).head
    periodFromPut.startDate shouldBe period.startDate
    periodFromPut.endDate shouldBe period.endDate
  }
}
