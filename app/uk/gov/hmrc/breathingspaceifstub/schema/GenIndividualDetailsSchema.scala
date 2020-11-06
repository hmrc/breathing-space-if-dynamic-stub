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

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import com.github.andyglow.json.JsonFormatter.format
import com.github.andyglow.jsonschema.AsValue
import json.{Json, Schema}
import json.schema.Version.Draft04
import uk.gov.hmrc.breathingspaceifstub.model._

object GenIndividualDetailsSchema extends App {

  val title = "Individual's Details"
  val description = "Schema of GET Individual's Details "

  // Full population ------------------------------------------------------------------------

  implicit val nameDataSchema: Schema[NameData] = Json.schema[NameData]
  implicit val addressDataSchema: Schema[AddressData] = Json.schema[AddressData]
  implicit val indicatorsSchema: Schema[Indicators] = Json.schema[Indicators]
  implicit val residencyDataSchema: Schema[ResidencyData] = Json.schema[ResidencyData]
  implicit val nameListSchema: Schema[NameList] = Json.schema[NameList]
  implicit val addressListSchema: Schema[AddressList] = Json.schema[AddressList]
  implicit val residencyListSchema: Schema[ResidencyList] = Json.schema[ResidencyList]

  implicit val detailsSchema: Schema[IndividualDetails] =
    Json.schema[IndividualDetails].withTitle(title).withDescription(s"$description(full population)")

  // Detail0 --------------------------------------------------------------------------------

  implicit val detail0Schema: Schema[IndividualDetail0] =
    Json.schema[IndividualDetail0].withTitle(title).withDescription(s"$description(filter #0)")

  // Detail1 --------------------------------------------------------------------------------

  implicit val detail1Schema: Schema[IndividualDetail1] =
    Json.schema[IndividualDetail1].withTitle(title).withDescription(s"$description(filter #1)")

  // ----------------------------------------------------------------------------------------

  val destinationFolder = "conf/schemas"

  print("\n")

  genSchemaFile(detail0Schema, "GET-Individual-Detail0-Schema.json")
  genSchemaFile(detail1Schema, "GET-Individual-Detail1-Schema.json")
  genSchemaFile(detailsSchema, "GET-Individual-Details-Schema.json")

  print(s"\n\nAll Json schema were generated Generated in $destinationFolder\n\n")

  private def genSchemaFile[T](schema: Schema[T], destinationFile: String): Unit = {
    val path = Paths.get(s"$destinationFolder/$destinationFile")

    Files.createDirectories(path.getParent)
    Files.write(path, format(AsValue.schema(schema, Draft04())).getBytes(StandardCharsets.UTF_8))
    print(s"\nGenerated a Json schema($destinationFile) in $destinationFolder")
  }
}
