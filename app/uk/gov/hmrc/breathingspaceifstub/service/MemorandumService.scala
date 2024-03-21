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

package uk.gov.hmrc.breathingspaceifstub.service

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import cats.implicits._
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.breathingspaceifstub.{AsyncResponse, Response}
import uk.gov.hmrc.breathingspaceifstub.model.{CustomErrors, Failure, Memorandum, NinoValidation}
import uk.gov.hmrc.breathingspaceifstub.model.BaseError._
import uk.gov.hmrc.breathingspaceifstub.repository.IndividualRepository

@Singleton
class MemorandumService @Inject()(
  individualRepository: IndividualRepository
)(implicit ec: ExecutionContext)
    extends NinoValidation
    with CustomErrors {

  def get(nino: String): AsyncResponse[Memorandum] = checkForCustomError(nino).getOrElse {
    stripNinoSuffixAndExecOp(nino, retrieveMemorandum)
  }

  private val retrieveMemorandum: String => AsyncResponse[Memorandum] =
    individualRepository
      .findIndividual(_)
      .map {
        _.fold[Response[Memorandum]](
          Failure(IDENTIFIER_NOT_IN_BREATHINGSPACE).asLeft
        ) { individual =>
          Memorandum(individual.periods.exists(p => p.endDate.isEmpty || p.endDate.exists(_.isAfter(LocalDate.now())))).asRight
        }
      }
}
