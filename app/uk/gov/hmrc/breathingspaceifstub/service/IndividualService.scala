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

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import cats.syntax.option._
import uk.gov.hmrc.breathingspaceifstub.{AsyncResponse, Response}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{IDENTIFIER_NOT_FOUND, INVALID_NINO, RESOURCE_NOT_FOUND}
import uk.gov.hmrc.breathingspaceifstub.repository.{Individual, Repository}

@Singleton
class IndividualService @Inject()(appConfig: AppConfig, individualRepository: Repository)(
  implicit ec: ExecutionContext
) extends NinoValidation {

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

  def count: Future[Int] = individualRepository.individualCount

  def delete(nino: String): AsyncResponse[Int] =
    stripNinoSuffixAndExecOp(nino, individualRepository.delete(_).collect {
      case Right(n) => if (n == 0) Left(Failure(RESOURCE_NOT_FOUND)) else Right(n)
    })

  def deleteAll: AsyncResponse[Int] = individualRepository.deleteAll

  def exists(nino: String): AsyncResponse[Boolean] =
    stripNinoSuffixAndExecOp(nino, individualRepository.exists(_).map(Right(_)))

  def listOfNinos: Future[List[String]] = individualRepository.listOfNinos

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): AsyncResponse[Unit] = {
    val detailsWithNino = individualDetails.copy(details = individualDetails.details.copy(nino = nino.some))
    stripNinoSuffixAndExecOp(nino, individualRepository.replaceIndividualDetails(_, detailsWithNino))
  }

  def retrieveUtr(nino: String): AsyncResponse[Option[String]] =
    stripNinoSuffixAndExecOp(
      nino,
      appConfig.onDevEnvironment,
      individualRepository
        .findIndividual(_)
        .map {
          _.fold[Response[Option[String]]](Left(Failure(IDENTIFIER_NOT_FOUND))) { individual =>
            Right(individual.individualDetails.indicators.map(_.utr).flatten)
          }
        }
    )
}
