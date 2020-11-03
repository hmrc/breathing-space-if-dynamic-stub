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

package uk.gov.hmrc.breathingspaceifstub.support

import scala.concurrent.Future

import akka.stream.Materializer
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers, Injecting}
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.controller.routes.{ErrorCodeController, IndividualController, PeriodsController}
import uk.gov.hmrc.breathingspaceifstub.model._

trait BaseISpec
  extends AnyFunSuite
    with BeforeAndAfterEach
    with BreathingSpaceTestSupport
    with DefaultAwaitTimeout
    with GuiceOneServerPerSuite
    with HeaderNames
    with Injecting
    with Matchers
    with OptionValues {

  val configProperties: Map[String, Any] = Map(
    "mongodb.uri" -> "mongodb://localhost:27017/breathing-space-it"
  )

  override lazy val app: Application = GuiceApplicationBuilder().configure(configProperties).build()

  implicit val materializer = inject[Materializer]

  override def beforeEach: Unit = await(deleteAll)

  def count: Future[Result] = call(Helpers.GET, IndividualController.count.url)
  def delete(nino: String): Future[Result] = call(Helpers.DELETE, IndividualController.delete(nino).url)
  def deleteAll: Future[Result] = call(Helpers.DELETE, IndividualController.deleteAll.url)
  def exists(nino: String): Future[Result] = call(Helpers.GET, IndividualController.exists(nino).url)
  def listOfNinos: Future[Result] = call(Helpers.GET, IndividualController.listOfNinos.url)

  def postIndividual(individualInRequest: IndividualInRequest): Future[Result] =
    call(Helpers.POST, IndividualController.postIndividual.url, Json.toJson(individualInRequest))

  def postIndividuals(individualsInRequest: IndividualsInRequest): Future[Result] =
    call(Helpers.POST, IndividualController.postIndividuals.url, Json.toJson(individualsInRequest))

  def postPeriods(nino: String, postPeriods: List[PostPeriodInRequest]): Future[Result] =
    call(Helpers.POST, PeriodsController.post(nino).url, Json.toJson(PostPeriodsInRequest(postPeriods)))

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): Future[Result] =
    call(Helpers.PUT, IndividualController.replaceIndividualDetails(nino).url, Json.toJson(individualDetails))

  def baseError(baseError: String): Future[Result] = call(Helpers.GET, ErrorCodeController.get(baseError).url)

  def call(method: String, url: String): Future[Result] = route(app, fakeRequest(method, url)).get
  def call(method: String, url: String, body: JsValue): Future[Result] =
    route(app, fakeRequest(method, url).withBody(body)).get

  def fakeRequest(method: String, url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, url).withHeaders(requestHeaders: _*)
}
