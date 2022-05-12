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

import cats.implicits.catsSyntaxOptionId
import play.api.http.Status.{CREATED, OK, TOO_MANY_REQUESTS}
import play.api.test.Helpers.{contentAsJson, status}
import uk.gov.hmrc.breathingspaceifstub.model.Memorandum
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

import java.time.LocalDate

class MemorandumControllerISpec extends BaseISpec {

  test("\"get\" (Memorandum) should return true when periods exist") {
    val individual = genIndividualInRequest(withPeriods = true)

    status(postIndividual(individual)) shouldBe CREATED
    val response = getMemorandum(individual.nino)
    status(response) shouldBe OK

    val memorandum = contentAsJson(response).as[Memorandum]
    memorandum shouldBe Memorandum(true)
  }

  test("\"get\" (Memorandum) should return true when periods has future date") {
    val futurePeriod = genPostPeriodInRequest(endDate = LocalDate.now().plusDays(1).some)
    val individual = genIndividualInRequest(periods = List(futurePeriod).some)

    status(postIndividual(individual)) shouldBe CREATED
    val response = getMemorandum(individual.nino)
    status(response) shouldBe OK

    val memorandum = contentAsJson(response).as[Memorandum]
    memorandum shouldBe Memorandum(true)
  }

  test("\"get\" (Memorandum) should return false when periods doesn't exist") {
    val individual = genIndividualInRequest(withPeriods = false)
    status(postIndividual(individual)) shouldBe CREATED

    val response = getMemorandum(individual.nino)
    status(response) shouldBe OK

    val memorandum = contentAsJson(response).as[Memorandum]
    memorandum shouldBe Memorandum(false)
  }

  test("\"get\" (Memorandum) should return an Error TOO_MANY_REQUESTS(429) for the nino BS000429C") {
    val nino = "BS000429C"
    val response = getMemorandum(nino)
    status(response) shouldBe TOO_MANY_REQUESTS
  }

}