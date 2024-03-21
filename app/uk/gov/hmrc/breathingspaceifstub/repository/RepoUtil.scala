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

import org.mongodb.scala.{BulkWriteResult, MongoWriteException}
import org.mongodb.scala.result.DeleteResult
import uk.gov.hmrc.breathingspaceifstub.Response
import uk.gov.hmrc.breathingspaceifstub.model.{Failure, WriteResult}
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{CONFLICTING_REQUEST, SERVER_ERROR}

object RepoUtil {

  val duplicateKey = 11000

  private[repository] def handleBulkWriteResult(
    writeResult: BulkWriteResult,
    duplicates: Int = 0
  ): Response[WriteResult] =
    Right(WriteResult(writeResult.getInsertedCount, duplicates, writeResult.getDeletedCount))

  private[repository] def handleDuplicateKeyError[T]: PartialFunction[Throwable, Response[T]] = {
    case exc: MongoWriteException if exc.getError.getCode == duplicateKey =>
      Left(Failure(CONFLICTING_REQUEST))
  }

  private[repository] def handleDeleteResult[T](deleteResult: DeleteResult, f: DeleteResult => T): Response[T] =
    if (deleteResult.wasAcknowledged()) {
      Right(f(deleteResult))
    } else Left(Failure(SERVER_ERROR))
}
