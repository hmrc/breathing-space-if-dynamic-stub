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

import java.time.LocalDate

import cats.syntax.option.none
import play.api.libs.json._

// Full population ----------------------------------------------------------------

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
object NameData {
  implicit val format: OFormat[NameData] = Json.format[NameData]

  val empty: NameData = NameData(
    nameSequenceNumber = none,
    nameType = none,
    titleType = none,
    requestedName = none,
    nameStartDate = none,
    nameEndDate = none,
    otherTitle = none,
    honours = none,
    firstForename = none,
    secondForename = none,
    surname = none
  )
}

final case class NameList(name: List[NameData])
object NameList { implicit val format: OFormat[NameList] = Json.format[NameList] }

// --------------------------------------------------------------------------------

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
object AddressData {
  implicit val format: OFormat[AddressData] = Json.format[AddressData]

  val empty: AddressData = AddressData(
    addressSequenceNumber = none,
    addressSource = none,
    countryCode = none,
    addressType = none,
    addressStatus = none,
    addressStartDate = none,
    addressEndDate = none,
    addressLastConfirmedDate = none,
    vpaMail = none,
    deliveryInfo = none,
    pafReference = none,
    addressLine1 = none,
    addressLine2 = none,
    addressLine3 = none,
    addressLine4 = none,
    addressLine5 = none,
    addressPostcode = none
  )
}

final case class AddressList(address: List[AddressData])
object AddressList { implicit val format: OFormat[AddressList] = Json.format[AddressList] }

// --------------------------------------------------------------------------------

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

object Indicators {

  implicit val indicatorsFormat: OFormat[Indicators] = new OFormat[Indicators] {
    override def reads(json: JsValue): JsResult[Indicators] = JsSuccess(
      Indicators(
        (json \ "manualCodingInd").asOpt[Int],
        (json \ "manualCodingReason").asOpt[Int],
        (json \ "manualCodingOther").asOpt[String],
        (json \ "manualCorrInd").asOpt[Int],
        (json \ "manualCorrReason").asOpt[String],
        (json \ "additionalNotes").asOpt[String],
        (json \ "deceasedInd").asOpt[Int],
        (json \ "s128Ind").asOpt[Int],
        (json \ "noAllowInd").asOpt[Int],
        (json \ "eeaCmnwthInd").asOpt[Int],
        (json \ "noRepaymentInd").asOpt[Int],
        (json \ "saLinkInd").asOpt[Int],
        (json \ "noATSInd").asOpt[Int],
        (json \ "taxEqualBenInd").asOpt[Int],
        (json \ "p2ToAgentInd").asOpt[Int],
        (json \ "digitallyExcludedInd").asOpt[Int],
        (json \ "bankruptcyInd").asOpt[Int],
        (json \ "bankruptcyFiledDate").asOpt[LocalDate],
        (json \ "utr").asOpt[String],
        (json \ "audioOutputInd").asOpt[Int],
        (json \ "welshOutputInd").asOpt[Int],
        (json \ "largePrintOutputInd").asOpt[Int],
        (json \ "brailleOutputInd").asOpt[Int],
        (json \ "specialistBusinessArea").asOpt[Int],
        (json \ "saStartYear").asOpt[String],
        (json \ "saFinalYear").asOpt[String],
        (json \ "digitalP2Ind").asOpt[Int]
      )
    )

    override def writes(o: Indicators): JsObject = Json.obj(
      "manualCodingInd" -> o.manualCodingInd,
      "manualCodingReason" -> o.manualCodingReason,
      "manualCodingInd" -> o.manualCodingInd,
      "manualCodingReason" -> o.manualCodingReason,
      "manualCodingOther" -> o.manualCodingOther,
      "manualCorrInd" -> o.manualCorrInd,
      "manualCorrReason" -> o.manualCorrReason,
      "additionalNotes" -> o.additionalNotes,
      "deceasedInd" -> o.deceasedInd,
      "s128Ind" -> o.s128Ind,
      "noAllowInd" -> o.noAllowInd,
      "eeaCmnwthInd" -> o.eeaCmnwthInd,
      "noRepaymentInd" -> o.noRepaymentInd,
      "saLinkInd" -> o.saLinkInd,
      "noATSInd" -> o.noATSInd,
      "taxEqualBenInd" -> o.taxEqualBenInd,
      "p2ToAgentInd" -> o.p2ToAgentInd,
      "digitallyExcludedInd" -> o.digitallyExcludedInd,
      "bankruptcyInd" -> o.bankruptcyInd,
      "bankruptcyFiledDate" -> o.bankruptcyFiledDate,
      "utr" -> o.utr,
      "audioOutputInd" -> o.audioOutputInd,
      "welshOutputInd" -> o.welshOutputInd,
      "largePrintOutputInd" -> o.largePrintOutputInd,
      "brailleOutputInd" -> o.brailleOutputInd,
      "specialistBusinessArea" -> o.specialistBusinessArea,
      "saStartYear" -> o.saStartYear,
      "saFinalYear" -> o.saFinalYear,
      "digitalP2Ind" -> o.digitalP2Ind
    )
  }

  val empty: Indicators = Indicators(
    manualCodingInd = none,
    manualCodingReason = none,
    manualCodingOther = none,
    manualCorrInd = none,
    manualCorrReason = none,
    additionalNotes = none,
    deceasedInd = none,
    s128Ind = none,
    noAllowInd = none,
    eeaCmnwthInd = none,
    noRepaymentInd = none,
    saLinkInd = none,
    noATSInd = none,
    taxEqualBenInd = none,
    p2ToAgentInd = none,
    digitallyExcludedInd = none,
    bankruptcyInd = none,
    bankruptcyFiledDate = none,
    utr = none,
    audioOutputInd = none,
    welshOutputInd = none,
    largePrintOutputInd = none,
    brailleOutputInd = none,
    specialistBusinessArea = none,
    saStartYear = none,
    saFinalYear = none,
    digitalP2Ind = none
  )
}

// --------------------------------------------------------------------------------

final case class ResidencyData(
  residencySequenceNumber: Option[Int],
  dateLeavingUK: Option[LocalDate],
  dateReturningUK: Option[LocalDate],
  residencyStatusFlag: Option[Int]
)
object ResidencyData { implicit val format: OFormat[ResidencyData] = Json.format[ResidencyData] }

final case class ResidencyList(residency: List[ResidencyData])
object ResidencyList { implicit val format: OFormat[ResidencyList] = Json.format[ResidencyList] }

// --------------------------------------------------------------------------------

final case class Details(
  nino: Option[String],
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
  crnIndicator: Option[Int]
)
object Details {
  implicit val format: OFormat[Details] = Json.format[Details]

  val empty: Details = Details(
    nino = none,
    ninoSuffix = none,
    accountStatusType = none,
    sex = none,
    dateOfEntry = none,
    dateOfBirth = none,
    dateOfBirthStatus = none,
    dateOfDeath = none,
    dateOfDeathStatus = none,
    dateOfRegistration = none,
    registrationType = none,
    adultRegSerialNumber = none,
    cesaAgentIdentifier = none,
    cesaAgentClientReference = none,
    permanentTSuffixCaseIndicator = none,
    currOptimisticLock = none,
    liveCapacitorInd = none,
    liveAgentInd = none,
    ntTaxCodeInd = none,
    mergeStatus = none,
    marriageStatusType = none,
    crnIndicator = none
  )
}

// --------------------------------------------------------------------------------

final case class IndividualDetails(
  details: Details,
  nameList: Option[NameList],
  addressList: Option[AddressList],
  indicators: Option[Indicators],
  residencyList: Option[ResidencyList]
)
object IndividualDetails {
  implicit val format: OFormat[IndividualDetails] = Json.format[IndividualDetails]

  val empty: IndividualDetails = IndividualDetails(
    details = Details.empty,
    nameList = none,
    addressList = none,
    indicators = none,
    residencyList = none
  )
}
