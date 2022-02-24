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

///*
// * Copyright 2022 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.breathingspaceifstub.repository
//
//import play.api.libs.functional.syntax.toFunctionalBuilderOps
//import play.api.libs.json._
//import reactivemongo.bson.BSONObjectID
//import uk.gov.hmrc.breathingspaceifstub.model.Underpayment
//import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.mongoEntity
//import reactivemongo.play.json.ImplicitBSONHandlers._
//import java.util.UUID
//
//final case class UnderpaymentRecord(
//  id: BSONObjectID = BSONObjectID.generate(),
//  underpayment: Option[Underpayment],
//  nino: String,
//  periodId: UUID
//)
//
//object UnderpaymentRecord {
//
//  implicit val jsonUnderpaymentFormat = Json.format[UnderpaymentRecord]
//  implicit val mongoUnderpaymentFormat = mongoEntity { jsonUnderpaymentFormat }
//
//  def parseToListOfUnderpaymentsDTOs(
//    rawUnderpayments: List[Underpayment],
//    nino: String,
//    periodId: String
//  ): List[UnderpaymentRecord] =
//    rawUnderpayments match {
//      case Nil => List(UnderpaymentRecord(nino = nino, periodId = UUID.fromString(periodId), underpayment = None))
//      case ls =>
//        ls.map(
//          u =>
//            UnderpaymentRecord(
//              nino = nino,
//              periodId = UUID.fromString(periodId),
//              underpayment = Some(Underpayment(u.taxYear, u.amount, u.source))
//            )
//        )
//    }
//}
