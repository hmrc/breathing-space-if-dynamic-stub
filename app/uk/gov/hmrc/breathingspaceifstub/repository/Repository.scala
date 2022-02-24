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

package uk.gov.hmrc.breathingspaceifstub.repository

import com.google.inject.ImplementedBy
import play.api.Logger
import reactivemongo.api.indexes.Index
import uk.gov.hmrc.breathingspaceifstub._
import uk.gov.hmrc.breathingspaceifstub.model._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[IndividualRepository])
trait Repository {

  def individualCount(implicit executionContent: ExecutionContext): Future[Int]

  def indexes: Seq[Index]

  def addIndividuals(individuals: Individuals): AsyncResponse[BulkWriteResult]

  def addIndividual(individual: Individual): AsyncResponse[Unit]

  def addPeriods(
    nino: String,
    consumerRequestId: UUID,
    maybeUtr: Option[String],
    periods: List[Period]
  ): AsyncResponse[Periods]

  def delete(nino: String): AsyncResponse[Int]

  def exists(nino: String): Future[Boolean]

  def deleteAll: AsyncResponse[Int]

  def findIndividual(nino: String): Future[Option[Individual]]

  def listOfNinos: Future[List[String]]

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): AsyncResponse[Unit]

  def updatePeriods(nino: String, periods: List[Period]): AsyncResponse[Periods]

//  def saveUnderpayments(underpayments: List[UnderpaymentRecord], logger: Logger): AsyncResponse[BulkWriteResult]

//  def removeUnderpayments(): AsyncResponse[Int]

  def removeByNino(nino: String): AsyncResponse[Int]

//  def removeByNinoAndPeriod(nino: String, periodId: UUID): AsyncResponse[Int]

//  def findUnderpayments(nino: String, periodId: String): Future[Option[List[UnderpaymentRecord]]]

//  def underpaymentCount(nino: String, periodId: UUID): AsyncResponse[Int]

  def deletePeriod(nino: String, periodId: UUID): AsyncResponse[Int]
}
