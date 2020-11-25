/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.breathingspaceifstub.model

import java.time.LocalDate

import cats.syntax.option._
import play.api.libs.json.Json
import uk.gov.hmrc.breathingspaceifstub.repository.Individual

final case class IndividualDetail0(
  nino: String,
  firstForename: Option[String],
  secondForename: Option[String],
  surname: Option[String],
  dateOfBirth: Option[LocalDate],
  addressLine1: Option[String],
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  addressLine5: Option[String],
  addressPostcode: Option[String],
  countryCode: Option[Int]
)

object IndividualDetail0 {
  val fields =
    "details(nino,dateOfBirth),nameList(name(firstForename,secondForename,surname)),addressList(address(addressLine1,addressLine2,addressLine3,addressLine4,addressLine5,addressPostcode,countryCode))"

  implicit val writes = Json.writes[IndividualDetail0]

  val empty = IndividualDetail0("", none, none, none, none, none, none, none, none, none, none, none)

  def apply(individual: Individual): IndividualDetail0 = {
    val individualDetails = individual.individualDetails
    val name = individualDetails.nameList.fold(empty) { nameList =>
      if (nameList.name.isEmpty) empty
      else {
        val data = nameList.name.head
        empty.copy(
          firstForename = data.firstForename,
          secondForename = data.secondForename,
          surname = data.surname
        )
      }
    }
    val address = individualDetails.addressList.fold(empty) { addressList =>
      if (addressList.address.isEmpty) empty
      else {
        val data = addressList.address.head
        empty.copy(
          addressLine1 = data.addressLine1,
          addressLine2 = data.addressLine2,
          addressLine3 = data.addressLine3,
          addressLine4 = data.addressLine4,
          addressLine5 = data.addressLine5,
          addressPostcode = data.addressPostcode,
          countryCode = data.countryCode
        )
      }
    }
    IndividualDetail0(
      nino = individual.nino,
      dateOfBirth = individualDetails.details.dateOfBirth,
      firstForename = name.firstForename,
      secondForename = name.secondForename,
      surname = name.surname,
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      addressLine5 = address.addressLine5,
      addressPostcode = address.addressPostcode,
      countryCode = address.countryCode
    )
  }
}
