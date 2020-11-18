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
import java.util.UUID

import play.api.libs.json.Json

// --------------------------------------------------------------------------------

final case class Period(
  periodID: UUID,
  startDate: LocalDate,
  endDate: Option[LocalDate]
)

object Period {
  implicit val format = Json.format[Period]
}

// --------------------------------------------------------------------------------

final case class Periods(periods: List[Period])

object Periods {
  implicit val format = Json.format[Periods]

  def apply(periodsInRequest: PeriodsInRequest): Periods =
    Periods(periodsInRequest.periods.map { periodInRequest =>
      Period(UUID.randomUUID, periodInRequest.startDate, periodInRequest.endDate)
    })

  def apply(periodsInRequest: List[PeriodInRequest]): List[Period] =
    periodsInRequest.map { periodInRequest =>
      Period(UUID.randomUUID, periodInRequest.startDate, periodInRequest.endDate)
    }
}
