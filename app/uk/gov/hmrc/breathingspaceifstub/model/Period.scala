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

final case class Period(
  periodID: UUID,
  startDate: LocalDate,
  endDate: Option[LocalDate]
)

object Period { implicit val format: OFormat[Period] = Json.format[Period] }

// --------------------------------------------------------------------------------

final case class Periods(periods: List[Period])

object Periods {
  implicit val format: OFormat[Periods] = Json.format[Periods]

  def fromPost(postPeriodsInRequest: List[PostPeriodInRequest]): List[Period] =
    postPeriodsInRequest.map { postPeriodInRequest =>
      Period(UUID.randomUUID, postPeriodInRequest.startDate, postPeriodInRequest.endDate)
    }

  def fromPut(putPeriodsInRequest: List[PutPeriodInRequest]): List[Period] =
    putPeriodsInRequest.map { putPeriodInRequest =>
      Period(putPeriodInRequest.periodID, putPeriodInRequest.startDate, putPeriodInRequest.endDate)
    }
}
