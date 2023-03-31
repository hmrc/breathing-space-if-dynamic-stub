/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.breathingspaceifstub.model.{Debt, IndividualDetails, Underpayment}
import uk.gov.hmrc.mongo.play.json.formats.{MongoBinaryFormats, MongoFormats, MongoJavatimeFormats, MongoUuidFormats}

trait ComponentFormats
    extends MongoBinaryFormats.Implicits
    with MongoJavatimeFormats.Implicits
    with MongoUuidFormats.Implicits {

  implicit val objectIdFormat = MongoFormats.objectIdFormat
  implicit val individualDetailsFormat = Json.format[IndividualDetails]
  implicit val underpaymentFormat = Json.format[Underpayment]
  implicit val debtFormat = Json.format[Debt]
  implicit val underpaymentRecordFormat = Json.format[UnderpaymentRecord]
  implicit val dateFormatInstant = MongoJavatimeFormats.localDateFormat

  implicit val underPaymentMongoFormat: OFormat[UnderpaymentRecord] = underpaymentRecordFormat
}

object ComponentFormats extends ComponentFormats
