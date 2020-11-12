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

  def empty(nino: String): IndividualDetail0 =
    IndividualDetail0(nino, none, none, none, none, none, none, none, none, none, none, none)

  def apply(individual: Individual): IndividualDetail0 =
    individual.individualDetails.fold(empty(individual.nino)) { details =>
      val name = details.nameList.fold(empty("")) { nameList =>
        val data = nameList.name.head
        IndividualDetail0(
          "",
          data.firstForename,
          data.secondForename,
          data.surname,
          none,
          none,
          none,
          none,
          none,
          none,
          none,
          none
        )
      }
      val address = details.addressList.fold(empty("")) { addressList =>
        val data = addressList.address.head
        IndividualDetail0(
          "",
          none,
          none,
          none,
          none,
          data.addressLine1,
          data.addressLine2,
          data.addressLine3,
          data.addressLine4,
          data.addressLine5,
          data.addressPostcode,
          data.countryCode
        )
      }
      IndividualDetail0(
        nino = individual.nino,
        firstForename = name.firstForename,
        secondForename = name.secondForename,
        surname = name.surname,
        dateOfBirth = details.dateOfBirth,
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
