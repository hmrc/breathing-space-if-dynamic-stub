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

import enumeratum._

sealed trait EndpointId extends EnumEntry

object EndpointId extends Enum[EndpointId] {

  case object BS_IndividualAll_DELETE extends EndpointId
  case object BS_Individual_DELETE extends EndpointId
  case object BS_IndividualExists_GET extends EndpointId
  case object BS_Individual_POST extends EndpointId
  case object BS_Individuals_POST extends EndpointId
  case object BS_Individual_PUT extends EndpointId
  case object BS_IndividualUtr_GET extends EndpointId
  case object BS_IndividualOverview_GET extends EndpointId
  case object BS_Debts_GET extends EndpointId
  case object BS_Details_GET extends EndpointId
  case object BS_FullDetails_GET extends EndpointId
  case object BS_Periods_GET extends EndpointId
  case object BS_Periods_POST extends EndpointId
  case object BS_Periods_PUT extends EndpointId
  case object BS_Periods_DELETE extends EndpointId
  case object BS_Memorandum_GET extends EndpointId

  case object BS_Underpayments_POST extends EndpointId
  case object BS_Underpayments_DELETE extends EndpointId
  case object BS_Underpayments_GET extends EndpointId

  override val values = findValues
}
