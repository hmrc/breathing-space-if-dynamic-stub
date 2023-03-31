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

import org.bson.types.ObjectId
import play.api.libs.json._
import uk.gov.hmrc.breathingspaceifstub.model.Underpayment
import uk.gov.hmrc.mongo.play.json.formats.{MongoFormats, MongoUuidFormats}

import java.util.UUID

final case class UnderpaymentRecord(
  _id: ObjectId = new ObjectId(),
  underpayment: Option[Underpayment],
  nino: String,
  periodId: UUID
)

object UnderpaymentRecord {

  implicit val objectIdFormat = MongoFormats.objectIdFormat
  implicit val jsonUnderpaymentFormat = Json.format[UnderpaymentRecord]
  implicit val uuidFormats = MongoUuidFormats.uuidFormat
  implicit val mongoFormat: OFormat[UnderpaymentRecord] = jsonUnderpaymentFormat

  def parseToListOfUnderpaymentsDTOs(
    rawUnderpayments: List[Underpayment],
    nino: String,
    periodId: String
  ): List[UnderpaymentRecord] =
    rawUnderpayments match {
      case Nil => List(UnderpaymentRecord(nino = nino, periodId = UUID.fromString(periodId), underpayment = None))
      case ls =>
        ls.map(
          u =>
            UnderpaymentRecord(
              nino = nino,
              periodId = UUID.fromString(periodId),
              underpayment = Some(Underpayment(u.taxYear, u.amount, u.source))
            )
        )
    }
}
