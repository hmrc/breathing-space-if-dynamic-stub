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

package uk.gov.hmrc.breathingspaceifstub.model

import scala.concurrent.Future
import scala.util.matching.Regex

import uk.gov.hmrc.breathingspaceifstub.{httpErrorMap, AsyncResponse}
import uk.gov.hmrc.breathingspaceifstub.model.BaseError._

trait CustomErrors {

  val ErrorPattern: Regex = "(BS000)(\\d{3})([A-Za-z])".r

  private def returnError[T](error: BaseError): Option[AsyncResponse[T]] = Some(Future.successful(Left(Failure(error))))

  def checkForCustomError[T](nino: String): Option[AsyncResponse[T]] = nino.toUpperCase match {
    case ErrorPattern(_, "403", _) => returnError(BREATHINGSPACE_EXPIRED)
    case ErrorPattern(_, "404", "A") => returnError(IDENTIFIER_NOT_IN_BREATHINGSPACE)
    case ErrorPattern(_, "404", "B") => returnError(RESOURCE_NOT_FOUND)
    case ErrorPattern(_, "404", "C") => returnError(NO_DATA_FOUND)
    case ErrorPattern(_, "404", "D") => returnError(IDENTIFIER_NOT_FOUND)
    case ErrorPattern(_, "409", _) => returnError(CONFLICTING_REQUEST)
    case ErrorPattern(_, status, _) => returnError(new HttpErrorCode(status.toInt, httpErrorMap(status.toInt)))
    case _ => None
  }
}
