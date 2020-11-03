package uk.gov.hmrc.breathingspaceifstub.controller

import java.time.LocalDate

import cats.syntax.option._
import play.api.http.Status.NOT_FOUND
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.CONFLICTING_REQUEST
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

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
    status(postIndividual(individual)) shouldBe OK
    contentAsString(exists(individual.nino)) shouldBe "true"
    status(delete(individual.nino)) shouldBe OK

    val response = count
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"count":0}"""
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
    status(postIndividual(individual)) shouldBe OK
    contentAsString(exists(individual.nino)) shouldBe "true"

    contentAsString(exists(genNino)) shouldBe "false"
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
    status(postIndividual(individual)) shouldBe OK
    contentAsString(exists(individual.nino)) shouldBe "true"
  }

  test("\"postIndividual\" should return 409(CONFLICT) when adding an existing Nino to the \"individual\" collection") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK
    contentAsString(exists(individual.nino)) shouldBe "true"

    val response = postIndividual(individual)
    status(response) shouldBe CONFLICT
    assert(contentAsString(response).startsWith(s"""{"failures":[{"code":"${CONFLICTING_REQUEST.entryName}"""))
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

  test("\"replaceIndividualDetails\" should successfully replace the individual details for the given Nino") {
    val individual1 = genIndividualInRequest()
    val individual2 = genIndividualInRequest()
    status(postIndividuals(IndividualsInRequest(List(individual1, individual2)))) shouldBe OK

    val individualDetails = IndividualDetails(
      dateOfBirth = LocalDate.now.some,
      nameList = NameList(List(NameData(firstForename = "Joe".some, surname = "Zawinul".some, none))).some
    )
    val response = replaceIndividualDetails(individual2.nino, individualDetails)
    status(response) shouldBe OK
  }

  test("\"replaceIndividualDetails\" should return 404(NOT_FOUND) when trying to update an unknown Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK
    contentAsString(exists(individual.nino)) shouldBe "true"

    val individualDetails = IndividualDetails(dateOfBirth = LocalDate.now.some)
    val response = replaceIndividualDetails(genNino, individualDetails)
    status(response) shouldBe NOT_FOUND
  }
}
