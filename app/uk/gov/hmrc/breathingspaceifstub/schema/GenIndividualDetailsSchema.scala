/*
 * Copyright 2021 HM Revenue & Customs
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

  val titleForDetails = "Individual's Details"
  val titleForIndividual = "Breathing Space Individual Documents"

  val destinationFolder = "conf/schemas"

  // Full population ------------------------------------------------------------------------

  implicit val detailsSchema: Schema[Details] = Json.schema[Details]
  implicit val indicatorsSchema: Schema[Indicators] = Json.schema[Indicators]

  implicit val nameDataSchema: Schema[NameData] = Json.schema[NameData]
  implicit val nameListSchema: Schema[NameList] = Json.schema[NameList]

  implicit val addressDataSchema: Schema[AddressData] = Json.schema[AddressData]
  implicit val addressListSchema: Schema[AddressList] = Json.schema[AddressList]

  implicit val residencyDataSchema: Schema[ResidencyData] = Json.schema[ResidencyData]
  implicit val residencyListSchema: Schema[ResidencyList] = Json.schema[ResidencyList]

  final case class ListOfNinos(ninos: List[String])

  /*
  private def genPostIndividualRequestSchema(): Unit = {
    val description = "Schema of POST Individual document request"
    genSchemaFile(
      Json.schema[IndividualInRequest].withTitle(titleForIndividual).withDescription(description),
      destinationFile = "POST-Individual-Request.json"
    )
  }

  private def genPostIndividualsRequestSchema(): Unit = {
    val description = "Schema of POST Individual documents request"
    genSchemaFile(
      Json.schema[IndividualsInRequest].withTitle(titleForIndividual).withDescription(description),
      destinationFile = "POST-Individuals-Request.json"
    )
  }
   */

  private def genPostIndividualsResponseSchema(): Unit = {
    val description = "Schema of POST Individual documents response"
    genSchemaFile(
      Json.schema[BulkWriteResult].withTitle(titleForIndividual).withDescription(description),
      destinationFile = "POST-Individuals-Response.json"
    )
  }

  private def genPutIndividualRequestSchema(): Unit = {
    val description = "Schema of PUT Individual Details document request"
    genSchemaFile(
      Json.schema[IndividualDetails].withTitle(titleForIndividual).withDescription(description),
      destinationFile = "PUT-Individual-Details-Request.json"
    )
  }

  private def genGetListOfNinosResponseSchema(): Unit = {
    val description = "Schema of List of Ninos of all existing Individual Documents response"
    genSchemaFile(
      Json.schema[ListOfNinos].withTitle(titleForIndividual).withDescription(description),
      destinationFile = "GET-List-Individual-Ninos-Response.json"
    )
  }

  private def genGetIndividualDetailsSchema(): Unit = {
    val description = "Schema of GET Individual's Details (full population)"
    genSchemaFile(
      Json.schema[IndividualDetails].withTitle(titleForDetails).withDescription(description),
      destinationFile = "GET-Individual-Details.json"
    )
  }

  private def genSchemaFile[T](schema: Schema[T], destinationFile: String): Unit = {
    val path = Paths.get(s"$destinationFolder/$destinationFile")

    Files.createDirectories(path.getParent)
    Files.write(path, format(AsValue.schema(schema, Draft04())).getBytes(StandardCharsets.UTF_8))
    print(s"\nGenerated a Json schema($destinationFile) in $destinationFolder")
  }

  print("\n")

//  genPostIndividualRequestSchema()
//  genPostIndividualsRequestSchema()
  genPostIndividualsResponseSchema()
  genPutIndividualRequestSchema()
  genGetListOfNinosResponseSchema()
  genGetIndividualDetailsSchema()

  print(s"\n\nAll Json schema were generated Generated in $destinationFolder\n\n")
}
