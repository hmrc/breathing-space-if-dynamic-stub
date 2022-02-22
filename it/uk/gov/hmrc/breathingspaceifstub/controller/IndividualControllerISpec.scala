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
import cats.syntax.option._
import play.api.http.Status.NOT_FOUND
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.CONFLICTING_REQUEST
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

import java.util.UUID

class IndividualControllerISpec extends BaseISpec {

  test("\"count\" should return the total number of documents in the \"individual\" collection") {
    val individual1 = genIndividualInRequest()
    val individual2 = genIndividualInRequest()
    status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

    val response = count
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"count":2}"""
  }

  test("\"delete(nino)\" should wipe out the \"individual\" document for the given Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

    status(postUnderpayments(individual.nino,
      periodId = "1519948e-8a54-11ec-8ed1-5bb13a0b0e93" ,
      Underpayments(List(u1)))) shouldBe OK

    status(delete(individual.nino)) shouldBe OK
    val response = count
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"count":0}"""
  }

  test("\"delete(nino)\" should wipe out the \"Underpayments\" for the given Nino") {
    val nino = "AS000001A"
    val periodId = "1519948e-8a54-11ec-8ed1-5bb13a0b0e93"
    status(postUnderpayments(nino,
      periodId,
      Underpayments(List(u1)))) shouldBe OK
    val response1 = countUnderpayments(nino, UUID.fromString(periodId))
    status(response1) shouldBe OK
    contentAsString(response1) shouldBe """{"count":1}"""

    status(delete(nino)) shouldBe OK

    val response2 = countUnderpayments(nino, UUID.fromString(periodId))
    status(response2) shouldBe OK
    contentAsString(response2) shouldBe """{"count":0}"""
  }

  test("\"delete(nino)\" should return 200(OK) if the provided Nino is unknown") {
    status(delete(genNino)) shouldBe OK
  }

  test("\"deleteAll\" should wipe out all documents in the \"individual\" collection") {
    val individual1 = genIndividualInRequest()
    val individual2 = genIndividualInRequest()
    status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK
    status(deleteAll) shouldBe OK

    val response = count
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"count":0}"""
  }

  test("\"exists(nino)\" should confirm if the \"individual\" collection contains or not the given Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

    contentAsString(exists(genNino)) shouldBe """{"exists":false}"""
  }

  test("\"exists(nino)\" should work even when the provided Nino includes the suffix") {
    val individual = IndividualInRequest(genNinoWithSuffix, none, none, none)
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""
  }

  test("\"exists(nino)\" should work when the provided Nino does not includes the suffix used for at creation") {
    val individual = IndividualInRequest(genNinoWithSuffix, none, none, none)
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino.substring(0, 8))) shouldBe """{"exists":true}"""
  }

  test("\"listOfNinos\" should return the list of all Ninos in the \"individual\" collection") {
    val individual1 = genIndividualInRequest()
    val individual2 = genIndividualInRequest()
    status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

    val response = listOfNinos
    status(response) shouldBe OK
    contentAsString(response) shouldBe s"""{"ninos":["${individual1.nino}","${individual2.nino}"]}"""
  }

  test("\"postIndividual\" should successfully add a new document to the \"individual\" collection") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""
  }

  test("\"postIndividual\" should successfully add a new document even for Nino with Suffix") {
    val individual = IndividualInRequest(genNinoWithSuffix, none, none, none)
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""
  }

  test("\"postIndividual\" should return 409(CONFLICT) when adding an existing Nino to the \"individual\" collection") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

    val response = postIndividual(individual)
    status(response) shouldBe CONFLICT
    assert(contentAsString(response).startsWith(s"""{"errors":[{"code":"${CONFLICTING_REQUEST.entryName}"""))
  }

  test("\"postIndividuals\" should successfully add all given new documents to the \"individual\" collection") {
    val individual1 = genIndividualInRequest()
    val individual2 = genIndividualInRequest()
    status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

    contentAsString(listOfNinos) shouldBe s"""{"ninos":["${individual1.nino}","${individual2.nino}"]}"""
    contentAsString(count) shouldBe """{"count":2}"""
  }

  test("\"postIndividuals\" should report if the given documents weren't all added to the \"individual\" collection") {
    val individual1 = genIndividualInRequest()
    val individual2 = genIndividualInRequest()
    val response = postIndividuals(IndividualsInRequest(List(individual1, individual2, individual1)))
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"successful":2,"duplicates":1,"errors":0}"""

    contentAsString(listOfNinos) shouldBe s"""{"ninos":["${individual1.nino}","${individual2.nino}"]}"""
    contentAsString(count) shouldBe """{"count":2}"""
  }

  test("\"postIndividuals\" should return 400(BAD_REQUEST) and INVALID_NINO if Ninos with suffix were given") {
    val individual1 = genIndividualInRequest()
    val individual2 = IndividualInRequest(genNinoWithSuffix, none, none, none)
    val response = postIndividuals(IndividualsInRequest(List(individual1, individual2, individual1)))
    status(response) shouldBe BAD_REQUEST
    (contentAsJson(response) \ "errors" \\ "code").head.as[String] shouldBe "INVALID_NINO"
  }

  test("\"replaceIndividualDetails\" should successfully replace the individual details for the given Nino") {
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

  test("\"replaceIndividualDetails\" should return 404(NOT_FOUND) when trying to update an unknown Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED
    contentAsString(exists(individual.nino)) shouldBe """{"exists":true}"""

    val individualDetails = IndividualDetails.empty.copy(details = Details.empty.copy(dateOfBirth = LocalDate.now.some))
    val response = replaceIndividualDetails(genNino, individualDetails)
    status(response) shouldBe NOT_FOUND
  }
}
