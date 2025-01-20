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

package uk.gov.hmrc.breathingspaceifstub.repository

import cats.implicits.catsSyntaxOptionId
import com.mongodb.client.model.Filters
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}
import uk.gov.hmrc.breathingspaceifstub.*
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.*
import uk.gov.hmrc.breathingspaceifstub.repository.RepoUtil.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoUuidFormats.Implicits.uuidFormat
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class IndividualRepository @Inject()(mongo: MongoComponent)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[Individual](
      mongoComponent = mongo,
      collectionName = "individual",
      domainFormat = Individual.mongoFormat,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("nino"),
          IndexOptions().name("Nino").unique(true)
        ),
        IndexModel(
          Indexes.ascending("periods.periodID"),
          IndexOptions().name("PeriodId").sparse(true)
        )
      ),
      extraCodecs = Seq(
        Codecs.playFormatCodec(uuidFormat),
        Codecs.playFormatCodec(Individual.mongoFormat),
        Codecs.playFormatCodec(ComponentFormats.individualDetailsFormat),
        Codecs.playFormatCodec(Period.format),
        Codecs.playFormatCodec(ComponentFormats.debtFormat),
        Codecs.playFormatCodec(ComponentFormats.dateFormatInstant)
      ),
      replaceIndexes = false
    ) {

  def addIndividuals(individuals: Individuals): AsyncResponse[WriteResult] = {
    val toInsert = removeDuplicateIndividuals(individuals)
    val duplicates = individuals.size - toInsert.size
    collection
      .bulkWrite(toInsert, BulkWriteOptions().ordered(false))
      .map(handleBulkWriteResult(_, duplicates))
      .head()
  }

  def addIndividual(individual: Individual): AsyncResponse[Unit] =
    collection
      .insertOne(individual)
      .map(_ => Right(unit))
      .recover(handleDuplicateKeyError)
      .head()

  def addPeriods(
    nino: String,
    consumerRequestId: UUID,
    maybeUtr: Option[String],
    periods: List[Period]
  ): AsyncResponse[Periods] = {
    val query = Filters.eq("nino", nino)
    collection
      .find(query)
      .map(_.periods)
      .head()
      .flatMap { res =>
        {
          val add2Set = Updates.addToSet("consumerRequestIds", consumerRequestId.toString)
          collection.updateOne(query, add2Set, UpdateOptions().upsert(false)).head().flatMap { result =>
            if (result.getModifiedCount == 1 && result.getMatchedCount == 1) {
              addPeriods(query, maybeUtr, periods ++ res)
            } else {
              Future.successful(
                Left(
                  Failure(
                    if (result.getMatchedCount == 0) IDENTIFIER_NOT_FOUND
                    else DUPLICATE_SUBMISSION
                  )
                )
              )
            }
          }
        }
      }
  }

  def count(): Future[Int] = collection.countDocuments().head().map(_.toInt)

  def delete(nino: String): AsyncResponse[Int] =
    collection
      .deleteOne(Filters.eq("nino", nino))
      .map {
        handleDeleteResult(_, _.getDeletedCount.toInt)
      }
      .head()

  def exists(nino: String): Future[Boolean] =
    collection
      .countDocuments(Filters.eq("nino", nino))
      .map { result =>
        result.toInt > 0
      }
      .head()

  def deleteAll(): AsyncResponse[Int] =
    collection
      .deleteMany(Filters.empty())
      .map {
        handleDeleteResult(_, _.getDeletedCount.toInt)
      }
      .head()

  def findIndividual(nino: String): Future[Option[Individual]] = collection.find(Filters.eq("nino", nino)).headOption()

  def listOfNinos: Future[List[String]] = collection.find(Filters.empty()).map(_.nino).toFuture().map(_.toList)

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): AsyncResponse[Unit] = {
    val query = Filters.eq("nino", nino)
    val modifier = Updates.set("individualDetails", individualDetails)
    collection.findOneAndUpdate(query, modifier).toFutureOption().flatMap {
      case Some(_) => Future.successful(Right(unit))
      case None => Future(Left(Failure(RESOURCE_NOT_FOUND)))
    }
  }

  def updatePeriods(nino: String, periods: List[Period]): AsyncResponse[Periods] =
    findIndividual(nino).flatMap {
      _.fold[AsyncResponse[Periods]](Future.successful(Left(Failure(IDENTIFIER_NOT_FOUND)))) { individual =>
        if (individual.periods.isEmpty) Future.successful(Right(Periods(periods = List.empty)))
        else updatePeriods(nino, periods, individual)
      }
    }

  def deletePeriod(nino: String, periodId: UUID): AsyncResponse[Int] =
    findIndividual(nino).flatMap {
      _.fold[AsyncResponse[Int]](Future.successful(Left(Failure(IDENTIFIER_NOT_FOUND)))) { individual =>
        val newPeriods = individual.periods.filterNot(p => p.periodID.equals(periodId))
        collection
          .updateOne(
            Filters.eq("nino", nino),
            Updates.set("periods", newPeriods)
          )
          .map(res => Right(res.getModifiedCount.toInt))
          .head()
      }
    }

  private def addPeriods(query: Bson, maybeUtr: Option[String], periods: List[Period]): AsyncResponse[Periods] = {
    val opOnPeriods = Updates.set("periods", periods)
    val update = maybeUtr.fold(opOnPeriods) { utr =>
      Updates.combine(
        Updates.set(s"individualDetails.indicators.utr", utr),
        opOnPeriods
      )
    }

    collection.updateOne(query, update, UpdateOptions().upsert(true)).toFutureOption().map {
      case Some(_) => Right(Periods(periods))
      case None => Left(Failure(IDENTIFIER_NOT_FOUND))
    }
  }

  private def removeDuplicateIndividuals(individuals: Individuals): Seq[InsertOneModel[Individual]] =
    individuals
      .foldLeft(Seq[Individual]())((unique, curr) => {
        if (!unique.exists(_.nino == curr.nino)) curr +: unique else unique
      })
      .reverse
      .map(InsertOneModel(_))

  private def updatePeriods(nino: String, periods: List[Period], individual: Individual): AsyncResponse[Periods] = {
    val periodsToNotUpdate = individual.periods.filterNot(p => periods.exists(_.periodID == p.periodID))
    if (periodsToNotUpdate.size == individual.periods.size) {
      Future.successful(Right(Periods(periods = periodsToNotUpdate)))
    } else {
      val newPeriods = periods ++ periodsToNotUpdate
      collection
        .updateOne(
          Filters.eq("nino", nino),
          Updates.set("periods", newPeriods)
        )
        .map { res =>
          if (res.getModifiedCount == 1) Right(Periods(periods = newPeriods))
          else {
            Left(Failure(SERVER_ERROR, s"Nino($nino)'s periods were not updated. UpdateWriteResult($res)".some))
          }
        }
        .head()
    }
  }
}
