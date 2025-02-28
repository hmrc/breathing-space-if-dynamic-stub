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

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

import play.api.libs.json.JsObject
import uk.gov.hmrc.breathingspaceifstub.{AsyncResponse, Response}
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.IDENTIFIER_NOT_FOUND
import uk.gov.hmrc.breathingspaceifstub.repository.IndividualRepository

@Singleton
class IndividualDetailsService @Inject() (individualRepository: IndividualRepository)(implicit
  ec: ExecutionContext
) extends NinoValidation {

  def getIndividualDetails(nino: String): AsyncResponse[IndividualDetails] =
    stripNinoSuffixAndExecOp(
      nino,
      individualRepository
        .findIndividual(_)
        .map(_.fold[Response[IndividualDetails]](Left(Failure(IDENTIFIER_NOT_FOUND))) { individual =>
          Right(individual.individualDetails)
        })
    )

  def getIndividualDetailsForBS(nino: String): AsyncResponse[JsObject] =
    stripNinoSuffixAndExecOp(
      nino,
      individualRepository
        .findIndividual(_)
        .map(_.fold[Response[JsObject]](Left(Failure(IDENTIFIER_NOT_FOUND))) { individual =>
          Right(IndividualDetailsForBS(individual))
        })
    )
}
