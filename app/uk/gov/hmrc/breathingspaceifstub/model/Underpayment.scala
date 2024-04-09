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

import play.api.libs.json.{Json, OFormat}
import java.lang.Integer.parseInt

// model

final case class Underpayment(taxYear: String, amount: Double, source: String)

final case class Underpayments(underPayments: List[Underpayment])

// Serializers

object Underpayment {
  implicit val format: OFormat[Underpayment] = Json.format[Underpayment]
}

object Underpayments {
  implicit val format: OFormat[Underpayments] = Json.format[Underpayments]
}

// custom validation for Underpayments

object Validators {
  def validateUnderpayment(u: Underpayment): Boolean =
    try {
      if ((u.source == "PAYE UP" || u.source == "SA UP" || u.source == "SA Debt") && parseInt(u.taxYear) > 1900) true
      else false
    } catch {
      case _: Exception => false
    }
}
