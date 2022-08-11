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

import java.util.UUID
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import com.mongodb.client.model.Filters
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.InsertOneModel
import uk.gov.hmrc.breathingspaceifstub.AsyncResponse
import uk.gov.hmrc.breathingspaceifstub.model.{Failure, Period, WriteResult}
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.CONFLICTING_REQUEST
import uk.gov.hmrc.breathingspaceifstub.repository.RepoUtil._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoUuidFormats.Implicits.uuidFormat

@Singleton
class UnderpaymentsRepository @Inject()(mongo: MongoComponent)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[UnderpaymentRecord](
      mongoComponent = mongo,
      collectionName = "underpayment",
      domainFormat = ComponentFormats.underPaymentMongoFormat,
      indexes = Seq(),
      extraCodecs = Seq(
        Codecs.playFormatCodec(uuidFormat),
        Codecs.playFormatCodec(Period.format),
        Codecs.playFormatCodec(ComponentFormats.underpaymentFormat)
      ),
      replaceIndexes = false
    ) {

  def saveUnderpayments(underpayments: List[UnderpaymentRecord]): AsyncResponse[WriteResult] = {

    val fhits: Future[List[UnderpaymentRecord]] =
      collection
        .find[UnderpaymentRecord](
          Filters.and(
            Filters.eq("nino", underpayments.head.nino),
            Filters.eq("periodId", underpayments.head.periodId)
          )
        )
        .toFuture()
        .map(res => res.toList)
    fhits.flatMap(
      hits =>
        if (hits.isEmpty) save(underpayments)
        else Future(Left(Failure(CONFLICTING_REQUEST)))
    )
  }

  private def save(underpayments: List[UnderpaymentRecord]): AsyncResponse[WriteResult] = {
    val models = underpayments.map(new InsertOneModel(_))
    collection
      .bulkWrite(models)
      .map { result =>
        if (result.getInsertedCount == 0) Left(Failure(CONFLICTING_REQUEST))
        else handleBulkWriteResult(result)
      }
      .recover(handleDuplicateKeyError)
      .head()
  }

  def removeUnderpayments(): AsyncResponse[Int] = remove(Filters.empty())

  def removeByNino(nino: String): AsyncResponse[Int] = remove(Filters.eq("nino", nino))

  def removeByNinoAndPeriodId(nino: String, periodId: UUID): AsyncResponse[Int] =
    remove(Filters.and(Filters.eq("nino", nino), Filters.eq("periodId", periodId)))

  private def remove(query: Bson): AsyncResponse[Int] =
    collection
      .deleteMany(query)
      .map(handleDeleteResult(_, _.getDeletedCount.toInt))
      .head()

  def findUnderpayments(nino: String, periodId: UUID): Future[Option[List[UnderpaymentRecord]]] =
    collection
      .find[UnderpaymentRecord](Filters.and(Filters.eq("nino", nino), Filters.eq("periodId", periodId)))
      .toFuture()
      .map(res => Some(res.toList))

  def count(nino: String, periodId: UUID): AsyncResponse[Int] =
    collection
      .countDocuments(
        Filters.and(Filters.eq("nino", nino), Filters.eq("periodId", periodId))
      )
      .head()
      .map(res => Right(res.toInt))
}
