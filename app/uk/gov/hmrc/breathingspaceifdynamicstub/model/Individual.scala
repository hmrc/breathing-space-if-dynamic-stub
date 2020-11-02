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

import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats._

final case class Individual(
  nino: String,
  individualDetails: Option[IndividualDetails],
  periods: List[Period] = List.empty,
  id: BSONObjectID = BSONObjectID.generate
)

object Individual {

  def apply(individualInRequest: IndividualInRequest): Individual =
    Individual(individualInRequest.nino, individualInRequest.individualDetails)

  def fromIndividualsInRequest(individualsInRequest: IndividualsInRequest): List[Individual] =
    individualsInRequest.individuals.map(Individual(_))

  implicit val jsonFormat = Json.format[Individual]

  implicit val mongoFormat = mongoEntity { jsonFormat }
}
