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

import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.breathingspaceifstub.AsyncResponse
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{IDENTIFIER_NOT_FOUND, INVALID_NINO}
import uk.gov.hmrc.breathingspaceifstub.repository.IndividualRepository
import uk.gov.hmrc.breathingspaceifstub.unit

@Singleton
class IndividualService @Inject()(individualRepository: IndividualRepository)(implicit ec: ExecutionContext)
    extends Nino {

  def addIndividual(individualInRequest: IndividualInRequest): AsyncResponse[Unit] =
    if (isValid(individualInRequest.nino)) individualRepository.addIndividual(Individual(individualInRequest))
    else Future.successful(Left(Failure(INVALID_NINO)))

  def addIndividuals(individualsInRequest: IndividualsInRequest): AsyncResponse[BulkWriteResult] =
    if (individualsInRequest.individuals.forall(individual => isValid(individual.nino))) {
      individualRepository.addIndividuals(Individual.fromIndividualsInRequest(individualsInRequest))
    } else Future.successful(Left(Failure(INVALID_NINO)))

  def count: Future[Int] = individualRepository.count

  def delete(nino: String): AsyncResponse[Unit] =
    individualRepository.delete(nino).collect {
      case Right(n) => if (n == 0) Left(Failure(IDENTIFIER_NOT_FOUND)) else Right(unit)
    }

  def deleteAll: AsyncResponse[Int] = individualRepository.deleteAll

  def exists(nino: String): Future[Boolean] = individualRepository.exists(nino)

  def listOfNinos: Future[List[String]] = individualRepository.listOfNinos

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): AsyncResponse[Unit] =
    individualRepository.replaceIndividualDetails(nino, individualDetails)
}
