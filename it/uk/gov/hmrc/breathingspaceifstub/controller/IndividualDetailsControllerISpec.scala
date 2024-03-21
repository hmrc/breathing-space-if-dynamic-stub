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

import java.time.LocalDate

import cats.syntax.option._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.UNKNOWN_DATA_ITEM
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class IndividualDetailsControllerISpec extends BaseISpec {

  test("\"get\" with query parameter \"fields\" filter for Breathing Space") {
    val dateOfBirth = LocalDate.now
    val firstForename = "Joe"
    val surname = "Zawinul"
    val nameType = 1
    val addressLine1 = "Some Street"
    val addressPostcode = "E20"
    val addressLine2 = "Another Street"
    val countryCode = 20
    val addressType = 1
    val welshOutputInd = 1

    val nameList = NameList(List(NameData.empty.copy(firstForename = firstForename.some, surname = surname.some,nameType = nameType.some)))
    val addressList = AddressList(List(
      AddressData.empty.copy(addressLine1 = addressLine1.some, addressPostcode = addressPostcode.some),
      AddressData.empty.copy(addressLine2 = addressLine2.some, countryCode = countryCode.some, addressType = addressType.some)
    ))

    val indicators = Indicators.empty.copy(welshOutputInd = welshOutputInd.some)

    val individualDetails = IndividualDetails.empty.copy(
      details = Details.empty.copy(dateOfBirth = dateOfBirth.some, sex = "M".some),
      nameList = nameList.some,
      addressList = addressList.some,
      indicators = indicators.some
    )

    val individual = genIndividualInRequest(individualDetails.some)
    status(postIndividual(individual)) shouldBe CREATED

    val response = getIndividualDetails(individual.nino, IndividualDetailsForBS.fields.some)
    status(response) shouldBe OK

    val expectedBody = Json.parse(
      s"""{
         |  "details":{"nino":"${individual.nino}","dateOfBirth":"$dateOfBirth"},
         |  "nameList":{"name":[{"firstForename":"$firstForename","surname":"$surname","nameType":$nameType}]},
         |  "addressList":{
         |    "address":[
         |      {"addressLine1":"$addressLine1","addressPostcode":"$addressPostcode"},
         |      {"addressLine2":"$addressLine2","countryCode":$countryCode,"addressType":$addressType}
         |    ]
         |  },
         |  "indicators":{
         |    "welshOutputInd": 1
         |  }
         |}"""
        .stripMargin
        .filter(ch => ch >= 32 && ch < 127)
    )

    assert(contentAsJson(response) == expectedBody)
  }

  test("\"get\" without query parameter \"fields\" (full population)") {
    val dateOfBirth = LocalDate.now
    val firstForename = "Joe"
    val surname = "Zawinul"
    val nameList = NameList(List(NameData.empty.copy(firstForename = firstForename.some, surname = surname.some)))

    val individualDetails = IndividualDetails.empty.copy(
      details = Details.empty.copy(dateOfBirth = dateOfBirth.some), nameList = nameList.some
    )

    val individual = genIndividualInRequest(individualDetails.some)
    status(postIndividual(individual)) shouldBe CREATED

    val response = getIndividualDetails(individual.nino)
    status(response) shouldBe OK

    val expectedBody =
      Json.parse(s"""{"details":{"nino":"${individual.nino}","dateOfBirth":"$dateOfBirth"},
          |"nameList":{"name":[{"firstForename":"$firstForename","surname":"$surname"}]}}"""
        .stripMargin
        .filterNot(_ == '\n')
      )

    assert(contentAsJson(response) == expectedBody)
  }

  test("\"get\" should work even when the given Nino includes the suffix") {
    val ninoWithSuffix = genNinoWithSuffix
    val ninoWithoutSuffix = ninoWithSuffix.substring(0, 8)
    val dateOfBirth = LocalDate.now
    val individual = IndividualInRequest(
      ninoWithoutSuffix,
      IndividualDetails.empty.copy(details = Details.empty.copy(dateOfBirth = dateOfBirth.some)).some,
      none, none
    )
    status(postIndividual(individual)) shouldBe CREATED

    val response = getIndividualDetails(ninoWithSuffix, IndividualDetailsForBS.fields.some)
    status(response) shouldBe OK

    val expectedBody = Json.parse(s"""{"details":{"nino":"${ninoWithoutSuffix}","dateOfBirth":"$dateOfBirth"}}""")
    assert(contentAsJson(response) == expectedBody)
  }

  test("\"get\" should return 404(NOT_FOUND) when trying to retrieve details for an unknown Nino") {
    status(getIndividualDetails(genNino)) shouldBe NOT_FOUND
  }

  test("\"get\" should return 422(UNPROCESSABLE_ENTITY) when \"fields\" provides an unknown \"detail\" value") {
    val response = getIndividualDetails(genNino, "?fields=details(dateOfBirth,cnrIndicator)".some)
    status(response) shouldBe UNPROCESSABLE_ENTITY
    (contentAsJson(response) \ "failures" \\ "code").head.as[String] shouldBe UNKNOWN_DATA_ITEM.entryName
  }
}
