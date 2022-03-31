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

package uk.gov.hmrc.breathingspaceifstub.model

import uk.gov.hmrc.breathingspaceifstub.AsyncResponse
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{BAD_GATEWAY, GATEWAY_TIMEOUT, SERVER_ERROR}

import scala.concurrent.Future
import scala.util.matching.Regex

object HttpErrorGenerator {
  val Nino500: Regex = "^B[A-Z]0+500[A-Z]".r
  val Nino502: Regex = "^B[A-Z]0+502[A-Z]".r
  val Nino503: Regex = "^B[A-Z]0+503[A-Z]".r
  val Nino504: Regex = "^B[A-Z]0+504[A-Z]".r

  def generateErrorByNino(nino: String): Option[AsyncResponse[Underpayments]] =
    nino match {
      case Nino500() => Some(Future.successful(Left(Failure(SERVER_ERROR))))
      case Nino502() => Some(Future.successful(Left(Failure(SERVER_ERROR))))
      case Nino503() => Some(Future.successful(Left(Failure(BAD_GATEWAY))))
      case Nino504() => Some(Future.successful(Left(Failure(GATEWAY_TIMEOUT))))
      case _ => None
    }
}
