/*
 * Copyright 2021 HM Revenue & Customs
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

import java.util.UUID
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

import uk.gov.hmrc.breathingspaceifstub.{AsyncResponse, Response}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{BREATHINGSPACE_ID_NOT_FOUND, IDENTIFIER_NOT_FOUND}
import uk.gov.hmrc.breathingspaceifstub.repository.IndividualRepository

@Singleton
class DebtsService @Inject()(appConfig: AppConfig, individualRepository: IndividualRepository)(
  implicit ec: ExecutionContext
) extends NinoValidation {

  def get(nino: String, periodId: UUID): AsyncResponse[Debts] =
    stripNinoSuffixAndExecOp(nino, appConfig.onDevEnvironment, retrieveDetbs(periodId))

  private def retrieveDetbs(periodId: UUID): String => AsyncResponse[Debts] =
    individualRepository
      .findIndividual(_)
      .map {
        _.fold[Response[Debts]](Left(Failure(IDENTIFIER_NOT_FOUND)))(
          individual =>
            if (individual.periods.exists(_.periodID == periodId)) Right(individual.debts)
            else Left(Failure(BREATHINGSPACE_ID_NOT_FOUND))
        )
      }
}
