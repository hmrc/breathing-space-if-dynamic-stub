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

import java.time.LocalDate

import cats.syntax.option._
import play.api.libs.json.Json

// --------------------------------------------------------------------------------

final case class NameData(
  firstForename: Option[String],
  surname: Option[String],
  secondForename: Option[String]
)

object NameData { implicit val format = Json.format[NameData] }

// --------------------------------------------------------------------------------

final case class NameList(name: List[NameData])

object NameList { implicit val format = Json.format[NameList] }

// --------------------------------------------------------------------------------

final case class IndividualDetails(
  dateOfBirth: Option[LocalDate] = none,
  crnIndicator: Option[Int] = none,
  nameList: Option[NameList] = none
)

object IndividualDetails { implicit val format = Json.format[IndividualDetails] }
