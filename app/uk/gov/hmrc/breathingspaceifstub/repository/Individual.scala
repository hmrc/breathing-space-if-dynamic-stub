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

import cats.syntax.option._
import org.bson.types.ObjectId
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.mongo.play.json.formats.MongoFormats

final case class Individual(
  nino: String,
  individualDetails: IndividualDetails,
  consumerRequestIds: List[String],
  periods: List[Period],
  debts: Debts,
  _id: ObjectId = new ObjectId()
)

object Individual {

  def apply(individualInRequest: IndividualInRequest): Individual = {
    val individualDetails =
      individualInRequest.individualDetails.fold {
        IndividualDetails(details = Details.empty.copy(nino = individualInRequest.nino.some), none, none, none, none)
      } { iD =>
        iD.copy(details = iD.details.copy(nino = individualInRequest.nino.some))
      }

    Individual(
      nino = individualInRequest.nino,
      individualDetails = individualDetails,
      consumerRequestIds = List.empty,
      periods = individualInRequest.periods.fold(List.empty[Period])(Periods.fromPost),
      debts = individualInRequest.debts.fold(List.empty[Debt])(identity)
    )
  }

  def fromIndividualsInRequest(individualsInRequest: IndividualsInRequest): Individuals =
    individualsInRequest.individuals.map(Individual(_))

  implicit val objectIdFormat: Format[ObjectId] = MongoFormats.objectIdFormat
  implicit val mongoFormat: OFormat[Individual] = Json.format[Individual]
}
