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

import cats.syntax.option._
import uk.gov.hmrc.breathingspaceifstub.AsyncResponse
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_NINO, RESOURCE_NOT_FOUND}
import uk.gov.hmrc.breathingspaceifstub.repository.{Individual, IndividualRepository}
import uk.gov.hmrc.breathingspaceifstub.unit

@Singleton
class IndividualService @Inject()(individualRepository: IndividualRepository)(implicit ec: ExecutionContext)
    extends NinoValidation {

  def addIndividual(individualInRequest: IndividualInRequest): AsyncResponse[Unit] =
    stripNinoSuffixAndExecOp(
      individualInRequest.nino,
      nino => individualRepository.addIndividual(Individual(individualInRequest.copy(nino = nino)))
    )

  val validateNino = (nino: String) => isValid(nino) && nino.length == 8

  def addIndividuals(individualsInRequest: IndividualsInRequest): AsyncResponse[BulkWriteResult] =
    if (individualsInRequest.individuals.forall(individual => validateNino(individual.nino))) {
      individualRepository.addIndividuals(Individual.fromIndividualsInRequest(individualsInRequest))
    } else Future.successful(Left(Failure(INVALID_NINO, "Maybe a Nino with Suffix? (Not valid for this action)".some)))

  def count: Future[Int] = individualRepository.count

  def delete(nino: String): AsyncResponse[Unit] =
    stripNinoSuffixAndExecOp(nino, individualRepository.delete(_).collect {
      case Right(n) => if (n == 0) Left(Failure(RESOURCE_NOT_FOUND)) else Right(unit)
    })

  def deleteAll: AsyncResponse[Int] = individualRepository.deleteAll

  def exists(nino: String): AsyncResponse[Boolean] =
    stripNinoSuffixAndExecOp(nino, individualRepository.exists(_).map(Right(_)))

  def listOfNinos: Future[List[String]] = individualRepository.listOfNinos

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): AsyncResponse[Unit] =
    stripNinoSuffixAndExecOp(nino, individualRepository.replaceIndividualDetails(_, individualDetails))
}
