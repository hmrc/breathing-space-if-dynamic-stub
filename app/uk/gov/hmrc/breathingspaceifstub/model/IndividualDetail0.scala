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

import cats.syntax.option._
import play.api.libs.json._
import uk.gov.hmrc.breathingspaceifstub.repository.Individual

object IndividualDetail0 {

  val Details = "details(nino,dateOfBirth)"
  val NameList = "nameList(name(firstForename,secondForename,surname))"
  val AddressList =
    "addressList(address(addressLine1,addressLine2,addressLine3,addressLine4,addressLine5,addressPostcode,countryCode))"

  val fields = s"$Details,$NameList,$AddressList"

  def apply(individual: Individual): JsObject = {
    val individualDetails = individual.individualDetails

    val details = JsObject(
      List(
        ("nino" -> Json.toJson(individual.nino)).some,
        individualDetails.details.dateOfBirth.map(dob => "dateOfBirth" -> Json.toJson(dob))
      ).flatten
    )

    val nameList: Option[JsObject] = individualDetails.nameList.flatMap { nL =>
      asOptArr(
        "name",
        Option(nL.name.flatMap { nameData =>
          asOptObj(
            List(
              asOptVal("firstForename", nameData.firstForename),
              asOptVal("secondForename", nameData.secondForename),
              asOptVal("surname", nameData.surname)
            )
          )
        })
      )
    }

    val addressList: Option[JsObject] = individualDetails.addressList.flatMap { aL =>
      asOptArr(
        "address",
        Option(aL.address.flatMap { addressData =>
          asOptObj(
            List(
              asOptVal("addressLine1", addressData.addressLine1),
              asOptVal("addressLine2", addressData.addressLine2),
              asOptVal("addressLine3", addressData.addressLine3),
              asOptVal("addressLine4", addressData.addressLine4),
              asOptVal("addressLine5", addressData.addressLine5),
              asOptVal("addressPostcode", addressData.addressPostcode),
              asOptVal("countryCode", addressData.countryCode)
            )
          )
        })
      )
    }

    JsObject(
      List(
        ("details" -> details).some,
        asOptVal("nameList", nameList),
        asOptVal("addressList", addressList)
      ).flatten
    )
  }

  private def asOptArr(name: String, optional: Option[List[JsObject]]): Option[JsObject] =
    optional.map(listOf => Json.obj(name -> listOf))

  private def asOptObj(list: List[Option[(String, JsValue)]]): Option[JsObject] =
    Option(list.flatten).filter(_.nonEmpty).map(JsObject(_))

  private def asOptVal[T](name: String, optional: Option[T])(implicit writes: Writes[T]): Option[(String, JsValue)] =
    optional.map(value => name -> Json.toJson((value)))
}
