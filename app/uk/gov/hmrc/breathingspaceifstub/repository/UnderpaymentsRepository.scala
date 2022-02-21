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

import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.breathingspaceifstub.AsyncResponse
import uk.gov.hmrc.breathingspaceifstub.model.BulkWriteResult
import uk.gov.hmrc.breathingspaceifstub.repository.RepoUtil._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UnderpaymentsRepository @Inject()(mongo: ReactiveMongoComponent)(implicit executionContext: ExecutionContext)
    extends ReactiveRepository[UnderpaymentRecord, BSONObjectID](
      collectionName = "underpayment",
      mongo = mongo.mongoConnector.db,
      domainFormat = UnderpaymentRecord.mongoUnderpaymentFormat,
      idFormat = ReactiveMongoFormats.objectIdFormats
    ) {

  def saveUnderpayments(underpayments: List[UnderpaymentRecord], logger: Logger): AsyncResponse[BulkWriteResult] = {
    logger.info(s"Received request to save ${underpayments.size} underpayments")

    val res = bulkInsert(underpayments)
      .map(handleBulkWriteResult)
      .recover(handleDuplicateKeyError[BulkWriteResult])

    logger.info(s"Saved ${underpayments.size} underpayments to disk")
    res
  }

  def removeUnderpayments(): AsyncResponse[Int] = removeAll().map(handleWriteResult(_, _.n))

  def removeByNino(nino: String): AsyncResponse[Int] = remove("nino" -> nino).map(handleWriteResult(_, _.n))

  def findUnderpayments(nino: String, periodId: String): Future[Option[List[UnderpaymentRecord]]] = {
    val fHits: Future[List[UnderpaymentRecord]] = find("nino" -> nino, "periodId" -> periodId)

    fHits.map {
      case Nil => None
      case ls => Some(ls)
    }
  }

  def underpaymentCount(nino: String, periodId: UUID): AsyncResponse[Int] = {
    val query = Json.obj("nino" -> nino, "periodId" -> periodId)
    count(query).map(n => Right(n))
  }
}
