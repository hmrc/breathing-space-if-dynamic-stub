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

import cats.syntax.option.*
import play.api.http.Status.NOT_FOUND
import play.api.test.Helpers.*
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.CONFLICTING_REQUEST
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

import java.time.LocalDate
import java.util.UUID

class IndividualControllerISpec extends BaseISpec {

  "Call" must {
    "count should return the total number of documents in the individual collection" in {
      val individual1 = genIndividualInRequest()
      val individual2 = genIndividualInRequest()
      status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

      val response = count
      status(response) shouldBe OK
      contentAsString(response) shouldBe """{"count":2}"""
    }
  }

  "Call" must {
    "delete(nino) should wipe out the individual document for the given Nino" in {
      val individual = genIndividualInRequest()
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

      status(
        postUnderpayments(individual.nino, periodId = "1519948e-8a54-11ec-8ed1-5bb13a0b0e93", Underpayments(List(u1)))
      ) shouldBe OK

      status(delete(individual.nino)) shouldBe OK
      val response = count
      status(response) shouldBe OK
      contentAsString(response) shouldBe """{"count":0}"""
    }
  }

  "Call" must {
    "delete(nino) should wipe out the Underpayments for the given Nino" in {
      val nino = "AS000001A"
      val periodId = "1519948e-8a54-11ec-8ed1-5bb13a0b0e93"
      status(postUnderpayments(nino, periodId, Underpayments(List(u1)))) shouldBe OK
      val response1 = countUnderpayments(nino, UUID.fromString(periodId))
      status(response1) shouldBe OK
      contentAsString(response1) shouldBe """{"count":1}"""

      status(delete(nino)) shouldBe OK

      val response2 = countUnderpayments(nino, UUID.fromString(periodId))
      status(response2) shouldBe OK
      contentAsString(response2) shouldBe """{"count":0}"""
    }
  }

  "Call" must {
    "delete(nino) should return 200(OK) if the provided Nino is unknown" in {
      status(delete(genNino)) shouldBe OK
    }
  }

  "Call" must {
    "deleteAll should wipe out all documents in the individual collection" in {
      val individual1 = genIndividualInRequest()
      val individual2 = genIndividualInRequest()
      status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK
      status(deleteAll()) shouldBe OK

      val response = count
      status(response) shouldBe OK
      contentAsString(response) shouldBe """{"count":0}"""
    }
  }

  "Call" must {
    "exists(nino) should confirm if the individual collection contains or not the given Nino" in {
      val individual = genIndividualInRequest()
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

      contentAsString(exists(genNino)) shouldBe """{"exists":false}"""
    }
  }

  "Call" must {
    "exists(nino) should work even when the provided Nino includes the suffix" in {
      val individual = IndividualInRequest(genNinoWithSuffix, none, none, none)
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""
    }
  }

  "Call" must {
    "exists(nino) should work when the provided Nino does not includes the suffix used for at creation" in {
      val individual = IndividualInRequest(genNinoWithSuffix, none, none, none)
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino.substring(0, 8))) shouldBe """{"exists":true}"""
    }
  }

  "Call" must {
    "listOfNinos should return the list of all Ninos in the individual collection" in {
      val individual1 = genIndividualInRequest()
      val individual2 = genIndividualInRequest()
      status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

      val response = listOfNinos
      status(response) shouldBe OK
      contentAsString(response) shouldBe s"""{"ninos":["${individual1.nino}","${individual2.nino}"]}"""
    }
  }

  "Call" must {
    "postIndividual should successfully add a new document to the individual collection" in {
      val individual = genIndividualInRequest()
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""
    }
  }

  "Call" must {
    "postIndividual should successfully add a new document even for Nino with Suffix" in {
      val individual = IndividualInRequest(genNinoWithSuffix, none, none, none)
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""
    }
  }

  "Call" must {
    "postIndividual should return 409(CONFLICT) when adding an existing Nino to the individual collection" in {
      val individual = genIndividualInRequest()
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

      val response = postIndividual(individual)
      status(response) shouldBe CONFLICT
      assert(
        contentAsString(response).startsWith(
          s"""{"errors":[{"code":"${CONFLICTING_REQUEST.getClass.getSimpleName.stripSuffix("$")}"""
        )
      )
    }
  }

  "Call" must {
    "postIndividuals should successfully add all given new documents to the individual collection" in {
      val individual1 = genIndividualInRequest()
      val individual2 = genIndividualInRequest()
      status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

      contentAsString(listOfNinos) shouldBe s"""{"ninos":["${individual1.nino}","${individual2.nino}"]}"""
      contentAsString(count) shouldBe """{"count":2}"""
    }
  }

  "Call" must {
    "postIndividuals should report if the given documents weren't all added to the individual collection" in {
      val individual1 = genIndividualInRequest()
      val individual2 = genIndividualInRequest()
      val response = postIndividuals(IndividualsInRequest(List(individual1, individual2, individual1)))
      status(response) shouldBe OK
      contentAsString(response) shouldBe """{"successful":2,"duplicates":1,"errors":0}"""

      contentAsString(listOfNinos) shouldBe s"""{"ninos":["${individual1.nino}","${individual2.nino}"]}"""
      contentAsString(count) shouldBe """{"count":2}"""
    }
  }

  "Call" must {
    "postIndividuals should return 400(BAD_REQUEST) and INVALID_NINO if Ninos with suffix were given" in {
      val individual1 = genIndividualInRequest()
      val individual2 = IndividualInRequest(genNinoWithSuffix, none, none, none)
      val response = postIndividuals(IndividualsInRequest(List(individual1, individual2, individual1)))
      status(response) shouldBe BAD_REQUEST
      (contentAsJson(response) \ "errors" \\ "code").head.as[String] shouldBe "INVALID_NINO"
    }
  }

  "Call" must {
    "replaceIndividualDetails should successfully replace the individual details for the given Nino" in {
      val individual1 = genIndividualInRequest()
      val individual2 = genIndividualInRequest()
      status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

      val individualDetails = IndividualDetails.empty.copy(
        details = Details.empty.copy(dateOfBirth = LocalDate.now.some),
        nameList = NameList(List(NameData.empty.copy(firstForename = "Joe".some, surname = "Zawinul".some))).some
      )
      val response = replaceIndividualDetails(individual2.nino, individualDetails)
      status(response) shouldBe NO_CONTENT
    }
  }

  "Call" must {
    "replaceIndividualDetails should return 404(NOT_FOUND) when trying to update an unknown Nino" in {
      val individual = genIndividualInRequest()
      status(postIndividual(individual)) shouldBe CREATED
      contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

      val individualDetails =
        IndividualDetails.empty.copy(details = Details.empty.copy(dateOfBirth = LocalDate.now.some))
      val response = replaceIndividualDetails(genNino, individualDetails)
      status(response) shouldBe NOT_FOUND
    }
  }

  "Call" must {
    "Get Overview should return a list of ninos with their corresponding period IDs" in {
      val individual = genIndividualInRequest(withPeriods = true)
      status(postIndividual(individual)) shouldBe CREATED
      val periodsResponse = getPeriods(individual.nino)

      val response = getOverview

      status(response) shouldBe OK
      val periodIDs = getPeriodIDsFromResponse(contentAsString(periodsResponse))
      contentAsString(response) shouldBe
        s"""{"periodsByNinos":[{"nino":"${individual.nino}","periods":["${periodIDs(0)}","${periodIDs(1)}"]}]}"""
    }
  }

  private def getPeriodIDsFromResponse(resp: String): List[String] = {
    val periodsRaw = resp.replaceAll(".*\\[\\{", "")
    val tokens = periodsRaw.split(",").toList
    val cleanTokens = tokens
      .map(_.replaceAll("[^a-zA-Z-0-9:]", ""))
      .filter(_.startsWith("periodID:"))
      .map(_.drop("periodID:".size))

    List(cleanTokens(0), cleanTokens(1))
  }
}
