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

package uk.gov.hmrc.breathingspaceifstub.service

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

import cats.syntax.option._
import uk.gov.hmrc.breathingspaceifstub.AsyncResponse
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.INVALID_JSON
import uk.gov.hmrc.breathingspaceifstub.repository.IndividualRepository

@Singleton
class PeriodsService @Inject()(individualRepository: IndividualRepository) extends Nino {

  def post(nino: String, postPeriods: PostPeriodsInRequest): AsyncResponse[Periods] =
    if (!postPeriods.periods.isEmpty) individualRepository.addPeriods(nino, Periods(postPeriods))
    else Future.successful(Left(Failure(INVALID_JSON, "List of periods is empty.".some)))
}