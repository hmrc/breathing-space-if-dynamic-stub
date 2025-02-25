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

import play.api.test.Helpers.*
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

import java.util.UUID

class DebtsControllerISpec extends BaseISpec {

  "Call" must {
    "get (Debts) should return all debts for an existing Nino, if any" in {
      val individual = genIndividualInRequest(withPeriods = true, withDebts = true)
      status(postIndividual(individual)) shouldBe CREATED

      val response = getDebts(individual.nino, retrievePeriodId(individual.nino))
      status(response) shouldBe OK

      val debts = contentAsJson(response).as[Debts]
      debts.size shouldBe 2

      debts.head shouldBe debt1
      debts.last shouldBe debt2
    }
  }

  "Call" must {
    "get (Debts) should return NO_DATA_FOUND(404) if no debts are found for the provided Nino" in {
      val individual = genIndividualInRequest(withPeriods = true)
      status(postIndividual(individual)) shouldBe CREATED

      val response = getDebts(individual.nino, retrievePeriodId(individual.nino))
      status(response) shouldBe NOT_FOUND
      assert(contentAsString(response).startsWith("""{"failures":[{"code":"NO_DATA_FOUND""""))
    }
  }

  "Call" must {
    "get (Debts) should return BREATHINGSPACE_ID_NOT_FOUND(404) if the provided periodId was not found" in {
      val individual = genIndividualInRequest()
      status(postIndividual(individual)) shouldBe CREATED

      val response = getDebts(individual.nino)
      status(response) shouldBe NOT_FOUND
      assert(contentAsString(response).startsWith("""{"failures":[{"code":"BREATHINGSPACE_ID_NOT_FOUND""""))
    }
  }

  "Call" must {
    "get (Debts) should report if the provided Nino is unknown" in {
      val response = getDebts(genNino)
      status(response) shouldBe NOT_FOUND
      assert(contentAsString(response).startsWith("""{"failures":[{"code":"IDENTIFIER_NOT_FOUND""""))
    }
  }

  private def retrievePeriodId(nino: String): UUID = {
    val periods = getPeriods(nino)
    status(periods) shouldBe OK
    contentAsJson(periods).as[Periods].periods.head.periodID
  }
}
