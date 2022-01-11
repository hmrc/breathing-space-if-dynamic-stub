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

import reactivemongo.api.commands.{MultiBulkWriteResult, WriteResult}
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.breathingspaceifstub.Response
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{CONFLICTING_REQUEST, SERVER_ERROR}
import uk.gov.hmrc.breathingspaceifstub.model.{BulkWriteResult, Failure}

object RepoUtil {

  val duplicateKey = 11000

  private[repository] def handleBulkWriteResult(writeResult: MultiBulkWriteResult): Response[BulkWriteResult] = {
    val duplicates = writeResult.writeErrors.count(_.code == duplicateKey)
    Right(BulkWriteResult(writeResult.n, duplicates, writeResult.totalN - writeResult.n - duplicates))
  }

  private[repository] def handleDuplicateKeyError[T]: PartialFunction[Throwable, Response[T]] = {
    case exc: DatabaseException if exc.code.contains(duplicateKey) =>
      Left(Failure(CONFLICTING_REQUEST))
  }

  private[repository] def handleWriteResult[T](writeResult: WriteResult, f: WriteResult => T): Response[T] =
    if (writeResult.ok) Right(f(writeResult))
    else Left(Failure(SERVER_ERROR, resolveWriteResultError(writeResult)))

  private def resolveWriteResultError(writeResult: WriteResult): Option[String] =
    Option(
      WriteResult
        .lastError(writeResult)
        .flatMap(_.errmsg.map(identity))
        .getOrElse("Unexpected error while inserting a document.")
    )
}
