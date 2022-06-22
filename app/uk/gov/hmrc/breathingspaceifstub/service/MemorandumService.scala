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

import cats.implicits._
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.BaseError._
import uk.gov.hmrc.breathingspaceifstub.model.{Failure, Memorandum, NinoValidation}
import uk.gov.hmrc.breathingspaceifstub.repository.IndividualRepository
import uk.gov.hmrc.breathingspaceifstub.{AsyncResponse, Response}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MemorandumService @Inject()(
  appConfig: AppConfig,
  individualRepository: IndividualRepository
)(implicit ec: ExecutionContext)
    extends NinoValidation {

  def get(nino: String): AsyncResponse[Memorandum] = nino match {
    case "BS000403B" => Future.successful(Left(Failure(BREATHINGSPACE_EXPIRED)))
    case "BS000404A" => Future.successful(Left(Failure(IDENTIFIER_NOT_IN_BREATHINGSPACE)))
    case "BS000404B" => Future.successful(Left(Failure(RESOURCE_NOT_FOUND)))
    case "BS000404C" => Future.successful(Left(Failure(NO_DATA_FOUND)))
    case "BS000404D" => Future.successful(Left(Failure(IDENTIFIER_NOT_FOUND)))
    case "BS000409B" => Future.successful(Left(Failure(CONFLICTING_REQUEST)))
    case "BS000500B" => Future.successful(Left(Failure(SERVER_ERROR)))
    case "BS000502B" => Future.successful(Left(Failure(BAD_GATEWAY)))
    case "BS000503B" => Future.successful(Left(Failure(SERVICE_UNAVAILABLE)))
    case _ => stripNinoSuffixAndExecOp(nino, retrieveMemorandum)
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
