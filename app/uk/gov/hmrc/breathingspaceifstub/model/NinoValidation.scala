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

package uk.gov.hmrc.breathingspaceifstub.model

import scala.concurrent.Future
import scala.util.Random

import uk.gov.hmrc.breathingspaceifstub.AsyncResponse
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.INVALID_NINO

trait NinoValidation {

  val validNinoFormat = "^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D ]?$"

  def isValid(nino: String): Boolean = nino.matches(validNinoFormat)

  def stripNinoSuffixAndExecOp[T](nino: String, f: String => AsyncResponse[T]): AsyncResponse[T] =
    if (isValid(nino)) {
      nino.length match {
        case 8 => f(nino)
        case 9 => f(nino.substring(0, 8))
        case _ => Future.successful(Left(Failure(INVALID_NINO)))
      }
    } else Future.successful(Left(Failure(INVALID_NINO)))

  lazy val valid1stChars = ('A' to 'Z').filterNot(List('D', 'F', 'I', 'Q', 'U', 'V').contains).map(_.toString)
  lazy val valid2ndChars = ('A' to 'Z').filterNot(List('D', 'F', 'I', 'O', 'Q', 'U', 'V').contains).map(_.toString)

  lazy val invalidPrefixes = List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ")

  lazy val validPrefixes = valid1stChars.flatMap(c => valid2ndChars.map(c + _)).filterNot(invalidPrefixes.contains(_))
  lazy val validSuffixes = ('A' to 'D').map(_.toString)

  lazy val random: Random = new Random

  def genNino: String = {
    val prefix = validPrefixes(random.nextInt(validPrefixes.length))
    val number = random.nextInt(1000000)
    f"$prefix$number%06d"
  }

  def genNinoWithSuffix: String = {
    val prefix = validPrefixes(random.nextInt(validPrefixes.length))
    val number = random.nextInt(1000000)
    val suffix = validSuffixes(random.nextInt(validSuffixes.length))
    f"$prefix$number%06d$suffix"
  }
}
