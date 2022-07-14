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
import play.api.http.Status._
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
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val response = getMemorandum(individual.nino)
    status(response) shouldBe OK

    val memorandum = contentAsJson(response).as[Memorandum]
    memorandum shouldBe Memorandum(false)
  }

  test("\"get\" (Memorandum) should return an Error BREATHINGSPACE_EXPIRED(403) for the nino BS000403B") {
    val nino = "BS000403B"
    val response = getMemorandum(nino)
    status(response) shouldBe FORBIDDEN
  }

  test("\"get\" (Memorandum) should return an Error RESOURCE_NOT_FOUND(404) for the nino BS000404B") {
    val nino = "BS000404B"
    val response = getMemorandum(nino)
    status(response) shouldBe NOT_FOUND
  }

  test("\"get\" (Memorandum) should return an Error NO_DATA_FOUND(404) for the nino BS000404C") {
    val nino = "BS000404C"
    val response = getMemorandum(nino)
    status(response) shouldBe NOT_FOUND
  }

  test("\"get\" (Memorandum) should return an Error IDENTIFIER_NOT_FOUND(404) for the nino BS000404D") {
    val nino = "BS000404D"
    val response = getMemorandum(nino)
    status(response) shouldBe NOT_FOUND
  }

  test("\"get\" (Memorandum) should return an Error CONFLICTING_REQUEST(409) for the nino BS000409B") {
    val nino = "BS000409B"
    val response = getMemorandum(nino)
    status(response) shouldBe CONFLICT
  }

  test("\"get\" (Memorandum) should return an Error INTERNAL_SERVER_ERROR(500) for the nino BS000500B") {
    val nino = "BS000500B"
    val response = getMemorandum(nino)
    status(response) shouldBe INTERNAL_SERVER_ERROR
  }

  test("\"get\" (Memorandum) should return an Error BAD_GATEWAY(502) for the nino BS000502B") {
    val nino = "BS000502B"
    val response = getMemorandum(nino)
    status(response) shouldBe BAD_GATEWAY
  }

  test("\"get\" (Memorandum) should return an Error SERVICE_UNAVAILABLE(503) for the nino BS000503B") {
    val nino = "BS000503B"
    val response = getMemorandum(nino)
    status(response) shouldBe SERVICE_UNAVAILABLE
  }
}