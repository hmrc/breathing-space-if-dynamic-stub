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

package uk.gov.hmrc.breathingspaceifstub.support

import java.util.UUID

import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes
import uk.gov.hmrc.breathingspaceifstub._
import uk.gov.hmrc.breathingspaceifstub.model._

trait BreathingSpaceTestSupport extends Nino {

  val randomUUID = UUID.randomUUID
  val randomUUIDAsString = randomUUID.toString
  val correlationId = randomUUID
  val correlationIdAsString = randomUUIDAsString

  val attendedUserId = "1234567"

  val genericErrorResponsePayload = """{"failures":[{"code":"AN_ERROR","reason":"An error message"}]}"""

  lazy val requestHeaders = List(
    CONTENT_TYPE -> MimeTypes.JSON,
    Header.CorrelationId -> correlationIdAsString,
    Header.OriginatorId -> Attended.DA2_BS_ATTENDED.toString,
    Header.UserId -> attendedUserId
  )

  def genIndividualInRequest(individualDetails: Option[IndividualDetails] = None): IndividualInRequest =
    IndividualInRequest(genNino, individualDetails)
}
