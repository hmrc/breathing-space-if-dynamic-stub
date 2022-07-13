/*
 * Copyright 2022 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}

import cats.syntax.option._
import uk.gov.hmrc.breathingspaceifstub.{AsyncResponse, Response}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{IDENTIFIER_NOT_FOUND, INVALID_JSON}
import uk.gov.hmrc.breathingspaceifstub.repository.IndividualRepository

@Singleton
class PeriodsService @Inject()(appConfig: AppConfig, individualRepository: IndividualRepository)(
  implicit ec: ExecutionContext
) extends NinoValidation {

  def get(nino: String): AsyncResponse[Periods] =
    stripNinoSuffixAndExecOp(
      nino,
      individualRepository
        .findIndividual(_)
        .map {
          _.fold[Response[Periods]](Left(Failure(IDENTIFIER_NOT_FOUND)))(
            individual => Right(Periods(individual.periods))
          )
        }
    )

  def post(maybeNino: String, postPeriods: PostPeriodsInRequest): AsyncResponse[Periods] =
    stripNinoSuffixAndExecOp(
      maybeNino,
      nino =>
        if (postPeriods.periods.nonEmpty) {
          individualRepository.addPeriods(
            nino,
            postPeriods.consumerRequestId,
            postPeriods.utr,
            Periods.fromPost(postPeriods.periods)
          )
        } else Future.successful(Left(Failure(INVALID_JSON, "List of periods is empty.".some)))
    )

  def put(maybeNino: String, putPeriods: PutPeriodsInRequest): AsyncResponse[Periods] =
    stripNinoSuffixAndExecOp(
      maybeNino,
      nino =>
        if (putPeriods.periods.isEmpty) Future.successful(Left(Failure(INVALID_JSON, "List of periods is empty.".some)))
        else individualRepository.updatePeriods(nino, Periods.fromPut(putPeriods.periods))
    )

  def delete(maybeNino: String, deletePeriodId: UUID): AsyncResponse[Int] =
    stripNinoSuffixAndExecOp(
      maybeNino,
      nino => individualRepository.deletePeriod(nino, deletePeriodId)
    )
}
