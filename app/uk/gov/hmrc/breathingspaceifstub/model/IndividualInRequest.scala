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

final case class IndividualInRequest(
  nino: String,
  individualDetails: Option[IndividualDetails],
  periods: Option[List[PostPeriodInRequest]],
  debts: Option[Debts]
)
object IndividualInRequest { implicit val format: OFormat[IndividualInRequest] = Json.format[IndividualInRequest] }

final case class IndividualsInRequest(
  individuals: List[IndividualInRequest]
)
object IndividualsInRequest { implicit val format: OFormat[IndividualsInRequest] = Json.format[IndividualsInRequest] }
