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

import cats.implicits.catsSyntaxOptionId
import play.api.http.Status.*
import play.api.test.Helpers.{contentAsJson, status}
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

import java.time.LocalDate

class MemorandumControllerISpec extends BaseISpec {

  "Call" must {
    "get (Memorandum) should return true when periods exist" in {
      val individual = genIndividualInRequest(withPeriods = true)

      status(postIndividual(individual)) shouldBe CREATED
      val response = getMemorandum(individual.nino)
      status(response) shouldBe OK

      val memorandum = contentAsJson(response).as[Memorandum]
      memorandum shouldBe Memorandum(true)
    }
  }

  "Call" must {
    "get (Memorandum) should return true when periods has future date" in {
      val futurePeriod = genPostPeriodInRequest(endDate = LocalDate.now().plusDays(1).some)
      val individual = genIndividualInRequest(periods = List(futurePeriod).some)

      status(postIndividual(individual)) shouldBe CREATED
      val response = getMemorandum(individual.nino)
      status(response) shouldBe OK

      val memorandum = contentAsJson(response).as[Memorandum]
      memorandum shouldBe Memorandum(true)
    }
  }

  "Call" must {
    "get (Memorandum) should return false when periods doesn't exist" in {
      val individual = genIndividualInRequest()
      status(postIndividual(individual)) shouldBe CREATED

      val response = getMemorandum(individual.nino)
      status(response) shouldBe OK

      val memorandum = contentAsJson(response).as[Memorandum]
      memorandum shouldBe Memorandum(false)
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error BREATHINGSPACE_EXPIRED(403) for the nino BS000403B" in {
      val nino = "BS000403B"
      val response = getMemorandum(nino)
      status(response) shouldBe FORBIDDEN
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error RESOURCE_NOT_FOUND(404) for the nino BS000404B" in {
      val nino = "BS000404B"
      val response = getMemorandum(nino)
      status(response) shouldBe NOT_FOUND
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error NO_DATA_FOUND(404) for the nino BS000404C" in {
      val nino = "BS000404C"
      val response = getMemorandum(nino)
      status(response) shouldBe NOT_FOUND
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error IDENTIFIER_NOT_FOUND(404) for the nino BS000404D" in {
      val nino = "BS000404D"
      val response = getMemorandum(nino)
      status(response) shouldBe NOT_FOUND
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error CONFLICTING_REQUEST(409) for the nino BS000409B" in {
      val nino = "BS000409B"
      val response = getMemorandum(nino)
      status(response) shouldBe CONFLICT
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error INTERNAL_SERVER_ERROR(500) for the nino BS000500B" in {
      val nino = "BS000500B"
      val response = getMemorandum(nino)
      status(response) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error BAD_GATEWAY(502) for the nino BS000502B" in {
      val nino = "BS000502B"
      val response = getMemorandum(nino)
      status(response) shouldBe BAD_GATEWAY
    }
  }

  "Call" must {
    "get (Memorandum) should return an Error SERVICE_UNAVAILABLE(503) for the nino BS000503B" in {
      val nino = "BS000503B"
      val response = getMemorandum(nino)
      status(response) shouldBe SERVICE_UNAVAILABLE
    }
  }

  "Call" must {
    "when static data is on get (Memorandum) should return true when periods exist and does not exist in mongo" in {
      val individual = genIndividualInRequest(withPeriods = true).copy(nino = "AA000333")

      val response = getMemorandum(individual.nino, staticDataOn = true)

      status(response) shouldBe OK

      val memorandum = contentAsJson(response).as[Memorandum]
      memorandum shouldBe Memorandum(true)
    }
  }

  "Call" must {
    "when static data is on get (Memorandum) should return true when periods exist and does exist in mongo" in {
      val individual = genIndividualInRequest(withPeriods = true).copy(nino = "AA000333")

      status(postIndividual(individual)) shouldBe CREATED

      val response = getMemorandum(individual.nino, staticDataOn = true)
      status(response) shouldBe OK
      val memorandum = contentAsJson(response).as[Memorandum]
      memorandum shouldBe Memorandum(true)
    }
  }

}
