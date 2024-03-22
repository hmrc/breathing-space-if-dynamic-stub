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

package support

import java.time.{LocalDate, ZonedDateTime}
import java.util.UUID

import cats.syntax.option._
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes
import uk.gov.hmrc.breathingspaceifstub._
import uk.gov.hmrc.breathingspaceifstub.model._

trait BreathingSpaceTestSupport extends NinoValidation {

  val randomUUID = UUID.randomUUID
  val randomUUIDAsString = randomUUID.toString
  val correlationId = randomUUID
  val correlationIdAsString = randomUUIDAsString

  val attendedUserId = "1234567"

  val withEndDate = true

  val genericErrorResponsePayload = """{"failures":[{"code":"AN_ERROR","reason":"An error message"}]}"""

  lazy val attendedRequestHeaders = List(
    CONTENT_TYPE -> MimeTypes.JSON,
    Header.CorrelationId -> correlationIdAsString,
    Header.OriginatorId -> Attended.DA2_BS_ATTENDED.toString,
    Header.UserId -> attendedUserId
  )

  lazy val unattendedRequestHeaders = List(
    CONTENT_TYPE -> MimeTypes.JSON,
    Header.CorrelationId -> correlationIdAsString,
    Header.OriginatorId -> Attended.DA2_BS_UNATTENDED.toString
  )

  lazy val memorandumRequestHeaders = List(
    CONTENT_TYPE -> MimeTypes.JSON,
    Header.CorrelationId -> correlationIdAsString,
    Header.OriginatorId -> Attended.DA2_PTA.toString
  )

  val individualDetails =
    IndividualDetails(
      details = Details.empty.copy(dateOfBirth = LocalDate.now.some),
      nameList = NameList(
        List(
          NameData.empty.copy(
            firstForename = "Joe".some,
            surname = "Zawinul".some,
            nameType = 1.some
          )
        )
      ).some,
      addressList = AddressList(
        List(
          AddressData.empty.copy(
            addressLine1 = "Somewhere St.".some,
            addressLine2 = "Flat 1 Lodge".some,
            addressLine3 = "London".some,
            addressPostcode = "CC12 4UE".some,
            addressType = 1.some
          )
        )
      ).some,
      indicators = none,
      residencyList = none
    )

  val debt1 = Debt(
    chargeReference = "ETMP ref01",
    chargeDescription = "100 chars long charge description as exist in ETMP",
    chargeAmount = 199999999.11,
    chargeCreationDate = LocalDate.now,
    chargeDueDate = LocalDate.now.plusMonths(1),
    none
  )

  val debt2 = Debt(
    chargeReference = "ETMP ref02",
    chargeDescription = "long charge 02 description as exist in ETMP",
    chargeAmount = 299999999.22,
    chargeCreationDate = LocalDate.now.plusDays(2),
    chargeDueDate = LocalDate.now.plusMonths(2),
    utrAssociatedWithCharge = "1234567890".some
  )

  val u1 = Underpayment("2011", 1010.23, "PAYE UP")
  val u2 = Underpayment("2012", 1010.23, "SA UP")
  val u3 = Underpayment("2013", 1010.23, "SA Debt")

  def genIndividualInRequest(
    individualDetails: Option[IndividualDetails] = None,
    withPeriods: Boolean = false,
    withDebts: Boolean = false
  ): IndividualInRequest =
    genIndividualInRequest(
      individualDetails,
      if (withPeriods) List(genPostPeriodInRequest(), genPostPeriodInRequest(withEndDate)).some else none,
      if (withDebts) List(debt1, debt2).some else none
    )

  def genIndividualInRequest(
    periods: Option[List[PostPeriodInRequest]]
  ): IndividualInRequest =
    genIndividualInRequest(none, periods, none)

  def genIndividualInRequest(
    individualDetails: Option[IndividualDetails],
    periods: Option[List[PostPeriodInRequest]],
    debts: Option[List[Debt]]
  ): IndividualInRequest =
    IndividualInRequest(
      genNino,
      individualDetails,
      periods,
      debts
    )

  def genPutPeriodInRequest(withEndDate: Boolean = false): PutPeriodInRequest =
    PutPeriodInRequest(
      UUID.randomUUID,
      LocalDate.now.minusMonths(2),
      if (withEndDate) LocalDate.now.some else none,
      ZonedDateTime.now.format(timestampFormatter)
    )

  def genPostPeriodInRequest(withEndDate: Boolean = false): PostPeriodInRequest =
    genPostPeriodInRequest(if (withEndDate) LocalDate.now.some else none)

  def genPostPeriodInRequest(endDate: Option[LocalDate]): PostPeriodInRequest =
    PostPeriodInRequest(
      LocalDate.now.minusMonths(2),
      endDate,
      ZonedDateTime.now.format(timestampFormatter)
    )
}
