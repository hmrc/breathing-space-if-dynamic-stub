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

package uk.gov.hmrc.breathingspaceifstub.repository

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import cats.syntax.flatMap._
import cats.syntax.option._
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.{MultiBulkWriteResult, WriteResult}
import reactivemongo.api.commands.FindAndModifyCommand.{Result, UpdateLastError}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json.collection.Helpers.idWrites
import uk.gov.hmrc.breathingspaceifstub._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

@Singleton()
class IndividualRepository @Inject()(mongo: ReactiveMongoComponent)(implicit executionContext: ExecutionContext)
    extends ReactiveRepository[Individual, BSONObjectID](
      collectionName = "individual",
      mongo = mongo.mongoConnector.db,
      domainFormat = Individual.mongoFormat,
      idFormat = ReactiveMongoFormats.objectIdFormats
    ) {

  override def indexes: Seq[Index] =
    Seq(
      Index(Seq("nino" -> IndexType.Ascending), name = Some("Nino"), unique = true),
      Index(Seq("periods.periodID" -> IndexType.Ascending), name = Some("PeriodId"), sparse = true)
    )

  def addIndividuals(individuals: Individuals): AsyncResponse[BulkWriteResult] =
    bulkInsert(individuals)
      .map(handleBulkWriteResult)
      .recover(handleDuplicateKeyError[BulkWriteResult])

  def addIndividual(individual: Individual): AsyncResponse[Unit] =
    insert(individual)
      .map(handleWriteResult(_, _ => unit))
      .recover(handleDuplicateKeyError[Unit])

  def addPeriods(nino: String, periods: List[Period]): AsyncResponse[Periods] = {
    val query = Json.obj("nino" -> nino)
    val update =
      Json.obj(
        "$push" ->
          Json.obj(
            "periods" ->
              Json.obj("$each" -> periods)
          )
      )
    findAndUpdate(query, update).map(handleUpdateResult(_, Periods(periods), IDENTIFIER_NOT_FOUND))
  }

  def delete(nino: String): AsyncResponse[Int] = remove("nino" -> nino).map(handleWriteResult(_, _.n))

  def exists(nino: String): Future[Boolean] =
    collection.find(Json.obj("nino" -> nino), none).one[Individual].map(_.fold(false)(_ => true))

  def deleteAll: AsyncResponse[Int] = removeAll().map(handleWriteResult(_, _.n))

  def findIndividual(nino: String): Future[Option[Individual]] =
    collection
      .find(Json.obj("nino" -> nino), none)
      .one[Individual]

  def listOfNinos: Future[List[String]] = findAll().map(_.map(_.nino))

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): AsyncResponse[Unit] = {
    val query = Json.obj("nino" -> nino)
    val update = Json.obj("$set" -> Json.obj("individualDetails" -> individualDetails))
    findAndUpdate(query, update).map(handleUpdateResult(_, unit, RESOURCE_NOT_FOUND))
  }

  def updatePeriods(nino: String, periods: List[Period]): AsyncResponse[Periods] =
    findIndividual(nino) >>= {
      _.fold[AsyncResponse[Periods]](Future.successful(Left(Failure(IDENTIFIER_NOT_FOUND)))) { individual =>
        if (individual.periods.isEmpty) Future.successful(Right(Periods(periods = List.empty)))
        else updatePeriods(nino, periods, individual)
      }
    }

  private def updatePeriods(nino: String, periods: List[Period], individual: Individual): AsyncResponse[Periods] = {
    val periodsToNotUpdate = individual.periods.filterNot(p => periods.find(_.periodID == p.periodID).isDefined)
    if (periodsToNotUpdate.size == individual.periods.size) {
      Future.successful(Right(Periods(periods = periodsToNotUpdate)))
    } else {
      val newPeriods = Periods(periods = periods ++ periodsToNotUpdate)
      collection
        .update(ordered = false)
        .one(
          Json.obj("nino" -> nino),
          Json.obj("$set" -> Json.obj("periods" -> Json.toJson(newPeriods)))
        )
        .map { uwr =>
          if (uwr.ok && uwr.nModified == 1) Right(newPeriods)
          else {
            Left(Failure(SERVER_ERROR, s"Nino($nino)'s periods were not updated. UpdateWriteResult($uwr)".some))
          }
        }
    }
  }

  val duplicateKey = 11000

  private def handleBulkWriteResult(writeResult: MultiBulkWriteResult): Response[BulkWriteResult] = {
    val duplicates = writeResult.writeErrors.count(_.code == duplicateKey)
    Right(BulkWriteResult(writeResult.n, duplicates, writeResult.totalN - writeResult.n - duplicates))
  }

  private def handleDuplicateKeyError[T]: PartialFunction[Throwable, Response[T]] = {
    case exc: DatabaseException if exc.code.contains(duplicateKey) =>
      Left(Failure(CONFLICTING_REQUEST))
  }

  private def handleUpdateResult[T](updateResult: Result[_], result: T, notFound: BaseError): Response[T] =
    updateResult.lastError.fold[Response[T]] {
      Left(Failure(SERVER_ERROR, "updateResult.lastError missing?".some))
    } { lastError =>
      if (lastError.updatedExisting) Right(result) else resolveUpdateLastError(lastError, notFound)
    }

  private def handleWriteResult[T](writeResult: WriteResult, f: WriteResult => T): Response[T] =
    if (writeResult.ok) Right(f(writeResult))
    else Left(Failure(SERVER_ERROR, resolveWriteResultError(writeResult)))

  private def resolveUpdateLastError[T](updateLastError: UpdateLastError, notFound: BaseError): Response[T] =
    Left(updateLastError.err.fold(Failure(notFound))(err => Failure(SERVER_ERROR, err.some)))

  private def resolveWriteResultError(writeResult: WriteResult): Option[String] =
    WriteResult
      .lastError(writeResult)
      .flatMap(_.errmsg.map(identity))
      .getOrElse("Unexpected error while inserting a document.")
      .some
}
