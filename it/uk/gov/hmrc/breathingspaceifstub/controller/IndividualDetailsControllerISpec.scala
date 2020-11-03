package uk.gov.hmrc.breathingspaceifstub.controller

import java.time.LocalDate

import cats.syntax.option._
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.UNKNOWN_DATA_ITEM
import uk.gov.hmrc.breathingspaceifstub.schema.{IndividualDetail0, IndividualDetail1}
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class IndividualDetailsControllerISpec extends BaseISpec {

  test("\"get\" with query parameter \"fields\" equal to detail #0)") {
    val dateOfBirth = LocalDate.now
    val individual = genIndividualInRequest(IndividualDetails(dateOfBirth.some).some)
    status(postIndividual(individual)) shouldBe OK

    val response = getIndividualDetails(individual.nino, IndividualDetail0.fields)
    status(response) shouldBe OK
    contentAsJson(response).toString shouldBe
      s"""{"nino":"${individual.nino}","dateOfBirth":"$dateOfBirth"}"""
  }

  test("\"get\" with query parameter \"fields\" equal to detail #1)") {
    val dateOfBirth = LocalDate.now
    val firstForename = "Joe"
    val surname = "Zawinul"
    val nameList = NameList(List(NameData(firstForename = firstForename.some, surname = surname.some, none)))

    val individualDetails = IndividualDetails(
      dateOfBirth = dateOfBirth.some,
      nameList = nameList.some
    )

    val individual = genIndividualInRequest(individualDetails.some)
    status(postIndividual(individual)) shouldBe OK

    val response = getIndividualDetails(individual.nino, IndividualDetail1.fields)
    status(response) shouldBe OK

    contentAsJson(response).toString shouldBe
      s"""{"nino":"${individual.nino}","dateOfBirth":"$dateOfBirth",
         |"nameList":{"name":[{"firstForename":"$firstForename","surname":"$surname"}]}}"""
        .stripMargin
        .filterNot(_ == '\n')
  }

  test("\"get\" should return 404(NOT_FOUND) when trying to update an unknown Nino") {
    status(getIndividualDetails(genNino, IndividualDetail0.fields)) shouldBe NOT_FOUND
  }

  test("\"get\" should return 422(UNPROCESSABLE_ENTITY) when \"fields\" provides an unknown detail value") {
    val response = getIndividualDetails(genNino, "?fields=details(dateOfBirth,cnrIndicator)")
    status(response) shouldBe UNPROCESSABLE_ENTITY
    (contentAsJson(response) \ "failures" \\ "code").head.as[String] shouldBe UNKNOWN_DATA_ITEM.entryName
  }

  test("\"get\" should return 422(UNPROCESSABLE_ENTITY) when the query parameter \"fields\" is missing") {
    val response = getIndividualDetails(genNino, "")
    status(response) shouldBe UNPROCESSABLE_ENTITY
    (contentAsJson(response) \ "failures" \\ "code").head.as[String] shouldBe UNKNOWN_DATA_ITEM.entryName
  }
}
