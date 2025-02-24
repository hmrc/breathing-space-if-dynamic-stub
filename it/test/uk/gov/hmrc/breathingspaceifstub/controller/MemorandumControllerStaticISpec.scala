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

package uk.gov.hmrc.breathingspaceifstub.controller

import org.scalatest.funsuite.AnyFunSuite
import play.api.Application
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers
import play.api.test.Helpers.{contentAsJson, status}
import uk.gov.hmrc.breathingspaceifstub.controller.routes.MemorandumController
import uk.gov.hmrc.breathingspaceifstub.model.Memorandum
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec
//import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.controller.routes.*
import uk.gov.hmrc.breathingspaceifstub.model.*

class MemorandumControllerStaticISpec extends BaseISpec {

  private val configProperties: Map[String, Any] = Map(
    "full-population-details-enabled" -> true,
    "mongodb.uri" -> "mongodb://localhost:27017/breathing-space-it",
    "feature.enableStaticData" -> true
  )

  private val app: Application = GuiceApplicationBuilder().configure(configProperties).build()

  test("\"get\" (Memorandum) should return true when periods exist and is static data and does not exist in mongo") {
    val individual = genIndividualInRequest(withPeriods = true).copy(nino = "AS000001")

    val response = {
      import play.api.test.Helpers.*
      route(app, memorandumFakeRequest(Helpers.GET, MemorandumController.get(individual.nino).url)).get
    }

    status(response) shouldBe OK

    val memorandum = contentAsJson(response).as[Memorandum]
    memorandum shouldBe Memorandum(true)
  }

  test("\"get\" (Memorandum) should return true when periods exist and is static data and does exist in mongo") {
    val individual = genIndividualInRequest(withPeriods = true).copy(nino = "AS000001")

    status(postIndividual(individual)) shouldBe CREATED

    val response = {
      import play.api.test.Helpers.*
      route(app, memorandumFakeRequest(Helpers.GET, MemorandumController.get(individual.nino).url)).get
    }
    status(response) shouldBe OK
    val memorandum = contentAsJson(response).as[Memorandum]
    memorandum shouldBe Memorandum(true)
  }

}
