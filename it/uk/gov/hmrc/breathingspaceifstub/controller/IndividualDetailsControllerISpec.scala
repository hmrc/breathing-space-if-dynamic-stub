package uk.gov.hmrc.breathingspaceifstub.controller

import java.time.LocalDate

import cats.syntax.option._
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.UNKNOWN_DATA_ITEM
import uk.gov.hmrc.breathingspaceifstub.schema.IndividualDetail1
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class IndividualDetailsControllerISpec extends BaseISpec {

  test("\"get\" with query parameter \"fields\" equal to detail #0)") {
    val dateOfBirth = LocalDate.now
    val individual = genIndividualInRequest(IndividualDetails.empty.copy(dateOfBirth = dateOfBirth.some).some)
    status(postIndividual(individual)) shouldBe CREATED

    val response = getIndividualDetails(individual.nino, IndividualDetail0.fields)
    status(response) shouldBe OK
    contentAsJson(response).toString shouldBe
      s"""{"nino":"${individual.nino}","dateOfBirth":"$dateOfBirth"}"""
  }

  test("\"get\" with query parameter \"fields\" equal to detail #1)") {
    val dateOfBirth = LocalDate.now
    val firstForename = "Joe"
    val surname = "Zawinul"
    val nameList = NameList(List(NameData.empty.copy(firstForename = firstForename.some, surname = surname.some)))

    val individualDetails = IndividualDetails.empty.copy(dateOfBirth = dateOfBirth.some, nameList = nameList.some)

    val individual = genIndividualInRequest(individualDetails.some)
    status(postIndividual(individual)) shouldBe CREATED

    val response = getIndividualDetails(individual.nino, IndividualDetail1.fields)
    status(response) shouldBe OK

    contentAsJson(response).toString shouldBe
      s"""{"nino":"${individual.nino}","dateOfBirth":"$dateOfBirth",
         |"nameList":{"name":[{"firstForename":"$firstForename","surname":"$surname"}]}}"""
        .stripMargin
        .filterNot(_ == '\n')
  }

  test("\"get\" without query parameter \"fields\" (full population)") {
    val dateOfBirth = LocalDate.now
    val firstForename = "Joe"
    val surname = "Zawinul"
    val nameList = NameList(List(NameData.empty.copy(firstForename = firstForename.some, surname = surname.some)))

    val individualDetails = IndividualDetails.empty.copy(dateOfBirth = dateOfBirth.some, nameList = nameList.some)

    val individual = genIndividualInRequest(individualDetails.some)
    status(postIndividual(individual)) shouldBe CREATED

    val response = getIndividualDetails(individual.nino, "")
    status(response) shouldBe OK

    contentAsJson(response).toString shouldBe
      s"""{"nino":"${individual.nino}","dateOfBirth":"$dateOfBirth",
         |"nameList":{"name":[{"firstForename":"$firstForename","surname":"$surname"}]}}"""
        .stripMargin
        .filterNot(_ == '\n')
  }

  test("\"get\" should work even when the given Nino includes the suffix") {
    val ninoWithSuffix = genNinoWithSuffix
    val ninoWithoutSuffix = ninoWithSuffix.substring(0, 8)
    val dateOfBirth = LocalDate.now
    val individual = IndividualInRequest(
      ninoWithoutSuffix,
      IndividualDetails.empty.copy(dateOfBirth = dateOfBirth.some).some
    )
    status(postIndividual(individual)) shouldBe CREATED

    val response = getIndividualDetails(ninoWithSuffix, IndividualDetail0.fields)
    status(response) shouldBe OK
    contentAsJson(response).toString shouldBe
      s"""{"nino":"${ninoWithoutSuffix}","dateOfBirth":"$dateOfBirth"}"""
  }

  test("\"get\" should return 404(NOT_FOUND) when trying to retrieve details for an unknown Nino") {
    status(getIndividualDetails(genNino, IndividualDetail0.fields)) shouldBe NOT_FOUND
  }

  test("\"get\" should return 422(UNPROCESSABLE_ENTITY) when \"fields\" provides an unknown \"detail\" value") {
    val response = getIndividualDetails(genNino, "?fields=details(dateOfBirth,cnrIndicator)")
    status(response) shouldBe UNPROCESSABLE_ENTITY
    (contentAsJson(response) \ "failures" \\ "code").head.as[String] shouldBe UNKNOWN_DATA_ITEM.entryName
  }
}
