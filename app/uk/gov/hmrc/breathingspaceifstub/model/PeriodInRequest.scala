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
import java.util.UUID

import play.api.libs.json.{Json, OFormat}

// --------------------------------------------------------------------------------

final case class PostPeriodInRequest(
  startDate: LocalDate,
  endDate: Option[LocalDate],
  pegaRequestTimestamp: String
)

object PostPeriodInRequest { implicit val format: OFormat[PostPeriodInRequest] = Json.format[PostPeriodInRequest] }

final case class PostPeriodsInRequest(consumerRequestId: UUID, utr: Option[String], periods: List[PostPeriodInRequest])
object PostPeriodsInRequest { implicit val format: OFormat[PostPeriodsInRequest] = Json.format[PostPeriodsInRequest] }

// --------------------------------------------------------------------------------

final case class PutPeriodInRequest(
  periodID: UUID,
  startDate: LocalDate,
  endDate: Option[LocalDate],
  pegaRequestTimestamp: String
)

object PutPeriodInRequest { implicit val format: OFormat[PutPeriodInRequest] = Json.format[PutPeriodInRequest] }

final case class PutPeriodsInRequest(periods: List[PutPeriodInRequest])
object PutPeriodsInRequest { implicit val format: OFormat[PutPeriodsInRequest] = Json.format[PutPeriodsInRequest] }
