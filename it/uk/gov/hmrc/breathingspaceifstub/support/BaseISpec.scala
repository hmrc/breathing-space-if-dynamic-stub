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

package uk.gov.hmrc.breathingspaceifstub.support

import java.util.UUID

import scala.concurrent.Future

import akka.stream.Materializer
import cats.syntax.option._
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
import uk.gov.hmrc.breathingspaceifstub.controller.routes._
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
    "full-population-details-enabled" -> true,
    "mongodb.uri" -> "mongodb://localhost:27017/breathing-space-it"
  )

  override lazy val app: Application = GuiceApplicationBuilder().configure(configProperties).build()

  implicit val materializer = inject[Materializer]

  override def beforeEach: Unit = await(deleteAll)

  // Production endpoints
  // ====================

  def getIndividualDetails(nino: String, fields: Option[String] = none): Future[Result] =
    attendedCall(Helpers.GET, IndividualDetailsController.get(nino, fields).url)

  def getDebts(nino: String, periodId: UUID = UUID.randomUUID): Future[Result] =
    attendedCall(Helpers.GET, DebtsController.get(nino, periodId).url)

  def getPeriods(nino: String): Future[Result] = attendedCall(Helpers.GET, PeriodsController.get(nino).url)

  def postPeriods(nino: String, periods: List[PostPeriodInRequest]): Future[Result] =
    postPeriods(nino, UUID.randomUUID, periods)

  def postPeriods(nino: String, consumerRequestId: UUID, postPeriods: List[PostPeriodInRequest]): Future[Result] =
    unattendedCall(
      Helpers.POST,
      PeriodsController.post(nino).url,
      Json.toJson(PostPeriodsInRequest(consumerRequestId, "1234567890".some, postPeriods))
    )

  def putPeriods(nino: String, putPeriods: List[PutPeriodInRequest]): Future[Result] =
    unattendedCall(Helpers.PUT, PeriodsController.put(nino).url, Json.toJson(PutPeriodsInRequest(putPeriods)))

  // Support endpoints
  // =================

  def count: Future[Result] = attendedCall(Helpers.GET, IndividualController.count.url)
  def delete(nino: String): Future[Result] = attendedCall(Helpers.DELETE, IndividualController.delete(nino).url)
  def deleteAll: Future[Result] = attendedCall(Helpers.DELETE, IndividualController.deleteAll.url)
  def exists(nino: String): Future[Result] = attendedCall(Helpers.GET, IndividualController.exists(nino).url)
  def listOfNinos: Future[Result] = attendedCall(Helpers.GET, IndividualController.listOfNinos.url)

  def postIndividual(individualInRequest: IndividualInRequest): Future[Result] =
    attendedCall(Helpers.POST, IndividualController.postIndividual.url, Json.toJson(individualInRequest))

  def postIndividuals(individualsInRequest: IndividualsInRequest): Future[Result] =
    attendedCall(Helpers.POST, IndividualController.postIndividuals.url, Json.toJson(individualsInRequest))

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): Future[Result] =
    attendedCall(Helpers.PUT, IndividualController.replaceIndividualDetails(nino).url, Json.toJson(individualDetails))

  // ==========================================================================================================

  def attendedCall(method: String, url: String): Future[Result] = route(app, attendedFakeRequest(method, url)).get
  def attendedCall(method: String, url: String, body: JsValue): Future[Result] =
    route(app, attendedFakeRequest(method, url).withBody(body)).get

  def unattendedCall(method: String, url: String, body: JsValue): Future[Result] =
    route(app, unattendedFakeRequest(method, url).withBody(body)).get

  def attendedFakeRequest(method: String, url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, url).withHeaders(attendedRequestHeaders: _*)

  def unattendedFakeRequest(method: String, url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, url).withHeaders(unattendedRequestHeaders: _*)
}
