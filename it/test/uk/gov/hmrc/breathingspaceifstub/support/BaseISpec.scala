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

package uk.gov.hmrc.breathingspaceifstub.support

import cats.syntax.option.*
import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Assertion, BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.*
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers, Injecting}
import uk.gov.hmrc.breathingspaceifstub.Header
import uk.gov.hmrc.breathingspaceifstub.controller.routes.*
import uk.gov.hmrc.breathingspaceifstub.model.*

import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait BaseISpec
    extends AnyWordSpec
    with BeforeAndAfterEach
    with BreathingSpaceTestSupport
    with DefaultAwaitTimeout
    with GuiceOneServerPerSuite
    with HeaderNames
    with Injecting
    with Matchers
    with OptionValues {

// FROM STATIC:-
  lazy val testServerAddress = s"http://localhost:$port"

  protected def checkCorrelationIDInResponse(
    response: Future[Result]
  )(implicit correlationHeaderValue: CorrelationId): Assertion =
    Await
      .result(response, Duration.Inf)
      .header
      .headers
      .get(Header.CorrelationId) shouldBe correlationHeaderValue.value

// END FROM STATIC

  val configProperties: Map[String, Any] = Map(
    "full-population-details-enabled" -> true,
    "mongodb.uri" -> "mongodb://localhost:27017/breathing-space-it",
    "feature.enableStaticData" -> false
  )

  override lazy val app: Application = GuiceApplicationBuilder().configure(configProperties).build()

  protected val appStaticDataOn: Application = {
    val configProperties: Map[String, Any] = Map(
      "full-population-details-enabled" -> true,
      "mongodb.uri" -> "mongodb://localhost:27017/breathing-space-it",
      "feature.enableStaticData" -> true
    )
    GuiceApplicationBuilder().configure(configProperties).build()
  }

  implicit val materializer: Materializer = inject[Materializer]

  override def beforeEach(): Unit =
    status(deleteAll()) shouldBe OK

  // Production endpoints
  // ====================

  def getIndividualDetails(nino: String, fields: Option[String] = none, staticDataOn: Boolean = false): Future[Result] =
    if (staticDataOn) {
      attendedCall(Helpers.GET, IndividualDetailsController.get(nino, fields).url, appStaticDataOn)
    } else {
      attendedCall(Helpers.GET, IndividualDetailsController.get(nino, fields).url)
    }

  def getDebts(nino: String, periodId: UUID = UUID.randomUUID, staticDataOn: Boolean = false): Future[Result] =
    if (staticDataOn) {
      attendedCall(Helpers.GET, DebtsController.get(nino, periodId).url, appStaticDataOn)
    } else {
      attendedCall(Helpers.GET, DebtsController.get(nino, periodId).url)
    }

  def getMemorandum(nino: String, staticDataOn: Boolean = false): Future[Result] =
    if (staticDataOn) {
      memorandumCall(Helpers.GET, MemorandumController.get(nino).url, appStaticDataOn)
    } else {
      memorandumCall(Helpers.GET, MemorandumController.get(nino).url)
    }

  def getPeriods(nino: String, staticDataOn: Boolean = false): Future[Result] =
    if (staticDataOn) {
      attendedCall(Helpers.GET, PeriodsController.get(nino).url, appStaticDataOn)
    } else {
      attendedCall(Helpers.GET, PeriodsController.get(nino).url)
    }
  /*

  def postPeriods(nino: String, periods: List[PostPeriodInRequest]): Future[Result] =
    postPeriods(nino, UUID.randomUUID, periods)

  def postPeriods(nino: String, consumerRequestId: UUID, postPeriods: List[PostPeriodInRequest]): Future[Result] =
    unattendedCall(
      Helpers.POST,
      PeriodsController.post(nino).url,
      Json.toJson(PostPeriodsInRequest(consumerRequestId, "1234567890".some, postPeriods))
    )
   */
  def postPeriods(
    nino: String,
    consumerRequestId: UUID = UUID.randomUUID(),
    postPeriods: List[PostPeriodInRequest],
    staticDataOn: Boolean = false
  ): Future[Result] =
    if (staticDataOn) {
      unattendedCall(
        Helpers.POST,
        PeriodsController.post(nino).url,
        Json.toJson(PostPeriodsInRequest(consumerRequestId, "1234567890".some, postPeriods)),
        appStaticDataOn
      )
    } else {
      unattendedCall(
        Helpers.POST,
        PeriodsController.post(nino).url,
        Json.toJson(PostPeriodsInRequest(consumerRequestId, "1234567890".some, postPeriods))
      )
    }

  def putPeriods(nino: String, putPeriods: List[PutPeriodInRequest], staticDataOn: Boolean = false): Future[Result] =
    if (staticDataOn) {
      unattendedCall(
        Helpers.PUT,
        PeriodsController.put(nino).url,
        Json.toJson(PutPeriodsInRequest(putPeriods)),
        appStaticDataOn
      )
    } else {
      unattendedCall(
        Helpers.PUT,
        PeriodsController.put(nino).url,
        Json.toJson(PutPeriodsInRequest(putPeriods))
      )
    }

  // Support endpoints
  // =================

  def count: Future[Result] = attendedCall(Helpers.GET, IndividualController.count.url)
  def delete(nino: String): Future[Result] = attendedCall(Helpers.DELETE, IndividualController.delete(nino).url)
  def deleteAll(): Future[Result] = attendedCall(Helpers.DELETE, IndividualController.deleteAll.url)
  def exists(nino: String): Future[Result] = attendedCall(Helpers.GET, IndividualController.exists(nino).url)
  def listOfNinos: Future[Result] = attendedCall(Helpers.GET, IndividualController.listOfNinos.url)

  def postIndividual(individualInRequest: IndividualInRequest): Future[Result] =
    attendedCall(Helpers.POST, IndividualController.postIndividual.url, Json.toJson(individualInRequest))

  def postIndividuals(individualsInRequest: IndividualsInRequest): Future[Result] =
    attendedCall(Helpers.POST, IndividualController.postIndividuals.url, Json.toJson(individualsInRequest))

  def replaceIndividualDetails(nino: String, individualDetails: IndividualDetails): Future[Result] =
    attendedCall(Helpers.PUT, IndividualController.replaceIndividualDetails(nino).url, Json.toJson(individualDetails))

  def postUnderpayments(nino: String, periodId: String, underpayments: Underpayments): Future[Result] =
    attendedCall(
      Helpers.POST,
      UnderpaymentsController.saveUnderpayments(nino, periodId).url,
      Json.toJson(underpayments)
    )

  def countUnderpayments(nino: String, periodId: UUID): Future[Result] =
    attendedCall(Helpers.GET, UnderpaymentsController.count(nino, periodId.toString).url)

  def deleteAllUnderpayments(): Future[Result] =
    attendedCall(Helpers.DELETE, UnderpaymentsController.clearUnderpayments.url)

  def getUnderpayments(nino: String, periodId: UUID): Future[Result] =
    attendedCall(Helpers.GET, UnderpaymentsController.get(nino, periodId).url)

  def getOverview: Future[Result] = attendedCall(Helpers.GET, IndividualController.getOverview.url)

  // ==========================================================================================================

  def attendedCall(method: String, url: String, appl: Application = app): Future[Result] =
    route(appl, attendedFakeRequest(method, url)).get

  def memorandumCall(method: String, url: String, appl: Application = app): Future[Result] =
    route(appl, memorandumFakeRequest(method, url)).get

  def attendedCall(method: String, url: String, body: JsValue): Future[Result] =
    route(app, attendedFakeRequest(method, url).withBody(body)).get

  def unattendedCall(method: String, url: String, body: JsValue, appl: Application = app): Future[Result] =
    route(appl, unattendedFakeRequest(method, url).withBody(body)).get

  def attendedFakeRequest(method: String, url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, url).withHeaders(attendedRequestHeaders: _*)

  def unattendedFakeRequest(method: String, url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, url).withHeaders(unattendedRequestHeaders: _*)

  def memorandumFakeRequest(method: String, url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, url).withHeaders(memorandumRequestHeaders: _*)
}
