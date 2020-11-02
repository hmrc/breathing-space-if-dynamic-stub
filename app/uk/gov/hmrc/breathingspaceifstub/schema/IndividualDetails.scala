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

package uk.gov.hmrc.breathingspaceifstub.schema

import java.time.LocalDate

// Detail0 --------------------------------------------------------------------------------

final case class IndividualDetail0(nino: String, dateOfBirth: LocalDate, crnIndicator: Int)
object IndividualDetail0 {
  val fields = "?fields=details(nino,dateOfBirth,cnrIndicator)"
}

// Detail1 --------------------------------------------------------------------------------

final case class NameDataForDetail1(firstForename: String, surname: String, secondForename: String)

final case class NameListForDetail1(name: List[NameDataForDetail1])

final case class IndividualDetail1(
  nino: String,
  dateOfBirth: LocalDate,
  nameList: NameListForDetail1
)
object IndividualDetail1 {
  val fields = "?fields=details(nino,dateOfBirth),namelist(name(firstForename,secondForename,surname))"
}

// Full population (as sent from NPS) -----------------------------------------------------

final case class NameData(
  nameSequenceNumber: Option[Int],
  nameType: Option[Int],
  titleType: Option[Int],
  requestedName: Option[String],
  nameStartDate: Option[LocalDate],
  nameEndDate: Option[LocalDate],
  otherTitle: Option[String],
  honours: Option[String],
  firstForename: Option[String],
  secondForename: Option[String],
  surname: Option[String]
)

final case class AddressData(
  addressSequenceNumber: Option[Int],
  addressSource: Option[Int],
  countryCode: Option[Int],
  addressType: Option[Int],
  addressStatus: Option[Int],
  addressStartDate: Option[LocalDate],
  addressEndDate: Option[LocalDate],
  addressLastConfirmedDate: Option[LocalDate],
  vpaMail: Option[Int],
  deliveryInfo: Option[String],
  pafReference: Option[String],
  addressLine1: Option[String],
  addressLine2: Option[String],
  addressLine3: Option[String],
  addressLine4: Option[String],
  addressLine5: Option[String],
  addressPostcode: Option[String]
)

final case class Indicators(
  manualCodingInd: Option[Int],
  manualCodingReason: Option[Int],
  manualCodingOther: Option[String],
  manualCorrInd: Option[Int],
  manualCorrReason: Option[String],
  additionalNotes: Option[String],
  deceasedInd: Option[Int],
  s128Ind: Option[Int],
  noAllowInd: Option[Int],
  eeaCmnwthInd: Option[Int],
  noRepaymentInd: Option[Int],
  saLinkInd: Option[Int],
  noATSInd: Option[Int],
  taxEqualBenInd: Option[Int],
  p2ToAgentInd: Option[Int],
  digitallyExcludedInd: Option[Int],
  bankruptcyInd: Option[Int],
  bankruptcyFiledDate: Option[LocalDate],
  utr: Option[String],
  audioOutputInd: Option[Int],
  welshOutputInd: Option[Int],
  largePrintOutputInd: Option[Int],
  brailleOutputInd: Option[Int],
  specialistBusinessArea: Option[Int],
  saStartYear: Option[String],
  saFinalYear: Option[String],
  digitalP2Ind: Option[Int]
)

final case class ResidencyData(
  residencySequenceNumber: Option[Int],
  dateLeavingUK: Option[LocalDate],
  dateReturningUK: Option[LocalDate],
  residencyStatusFlag: Option[Int]
)

final case class NameList(name: List[NameData])
final case class AddressList(address: List[AddressData])
final case class ResidencyList(residency: List[ResidencyData])

// --------------------------------------------------------------------------------

final case class IndividualDetails_FullPopulation(
  nino: String,
  ninoSuffix: Option[String],
  accountStatusType: Option[Int],
  sex: Option[String],
  dateOfEntry: Option[LocalDate],
  dateOfBirth: Option[LocalDate],
  dateOfBirthStatus: Option[Int],
  dateOfDeath: Option[LocalDate],
  dateOfDeathStatus: Option[Int],
  dateOfRegistration: Option[LocalDate],
  registrationType: Option[Int],
  adultRegSerialNumber: Option[String],
  cesaAgentIdentifier: Option[String],
  cesaAgentClientReference: Option[String],
  permanentTSuffixCaseIndicator: Option[Int],
  currOptimisticLock: Option[Int],
  liveCapacitorInd: Option[Int],
  liveAgentInd: Option[Int],
  ntTaxCodeInd: Option[Int],
  mergeStatus: Option[Int],
  marriageStatusType: Option[Int],
  crnIndicator: Option[Int],
  nameList: Option[NameList],
  addressList: Option[AddressList],
  indicators: Option[Indicators],
  residencyList: Option[ResidencyList]
)