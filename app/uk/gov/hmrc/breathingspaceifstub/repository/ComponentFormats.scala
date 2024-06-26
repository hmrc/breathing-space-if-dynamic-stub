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

import org.bson.types.ObjectId
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.breathingspaceifstub.model.{Debt, IndividualDetails, Underpayment}
import uk.gov.hmrc.mongo.play.json.formats.{MongoBinaryFormats, MongoFormats, MongoJavatimeFormats, MongoUuidFormats}
import java.time.LocalDate

trait ComponentFormats
    extends MongoBinaryFormats.Implicits
    with MongoJavatimeFormats.Implicits
    with MongoUuidFormats.Implicits {

  implicit val objectIdFormat: Format[ObjectId] = MongoFormats.objectIdFormat
  implicit val individualDetailsFormat: OFormat[IndividualDetails] = Json.format[IndividualDetails]
  implicit val underpaymentFormat: OFormat[Underpayment] = Json.format[Underpayment]
  implicit val debtFormat: OFormat[Debt] = Json.format[Debt]
  implicit val underpaymentRecordFormat: OFormat[UnderpaymentRecord] = Json.format[UnderpaymentRecord]
  implicit val dateFormatInstant: Format[LocalDate] = MongoJavatimeFormats.localDateFormat
  implicit val underPaymentMongoFormat: OFormat[UnderpaymentRecord] = underpaymentRecordFormat
}

object ComponentFormats extends ComponentFormats
