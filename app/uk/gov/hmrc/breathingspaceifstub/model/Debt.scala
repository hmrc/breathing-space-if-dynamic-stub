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

package uk.gov.hmrc.breathingspaceifstub.model

import java.time.LocalDate
import scala.math.BigDecimal.RoundingMode
import play.api.libs.json._

case class Debt(
  chargeReference: String,
  chargeDescription: String,
  chargeAmount: BigDecimal,
  chargeCreationDate: LocalDate,
  chargeDueDate: LocalDate,
  utrAssociatedWithCharge: Option[String]
)

object Debt {
  implicit val reads: Reads[Debt] = Json.reads[Debt]

  implicit val writes: Writes[Debt] = new Writes[Debt] {
    override def writes(debt: Debt): JsObject = {
      val fields = List(
        "chargeReference" -> JsString(debt.chargeReference),
        "chargeDescription" -> JsString(debt.chargeDescription),
        "chargeAmount" -> JsNumber(debt.chargeAmount.setScale(2, RoundingMode.HALF_EVEN)),
        "chargeCreationDate" -> Json.toJson(debt.chargeCreationDate),
        "chargeDueDate" -> Json.toJson(debt.chargeDueDate)
      )
      JsObject(debt.utrAssociatedWithCharge.fold(fields) { utrAwC =>
        fields :+ ("utrAssociatedWithCharge" -> Json.toJson(utrAwC))
      })
    }
  }
}
