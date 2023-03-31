/*
 * Copyright 2023 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.breathingspaceifstub.Response
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId._
import uk.gov.hmrc.breathingspaceifstub.service.{IndividualService, UnderpaymentsService}

@Singleton()
class IndividualController @Inject()(
  individualService: IndividualService,
  cc: ControllerComponents,
  underpaymentsService: UnderpaymentsService
)(implicit val ec: ExecutionContext)
    extends AbstractBaseController(cc) {

  val count: Action[Unit] = Action.async(withoutBody) { _ =>
    individualService.count.map(count => Ok(Json.obj("count" -> count)))
  }

  def delete(nino: String): Action[Unit] = Action.async(withoutBody) { _ =>
    val fIndividualDel = individualService.delete(nino)
    val fUnderpaymentsDel = underpaymentsService.removeUnderpaymentFor(nino)

    def normalise[A](either: Either[Failure, A]): Int = either match {
      case Right(value) => Integer.parseInt(value.toString)
      case _ => 0
    }

    val fRes = for {
      e1 <- fIndividualDel
      e2 <- fUnderpaymentsDel
    } yield (e1, e2)

    fRes.map {
      case (e1, e2) =>
        Ok(
          Json.obj(
            "individualsDeleted" -> normalise(e1),
            "underpaymentsDeleted" -> normalise(e2)
          )
        )
    }
  }

  val deleteAll: Action[Unit] = Action.async(withoutBody) { implicit request =>
    implicit val requestId: RequestId = RequestId(BS_IndividualAll_DELETE)
    individualService.deleteAll.map(_.fold(logAndGenErrorResult, count => Ok(Json.obj("deleted" -> count))))
  }

  def exists(nino: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    implicit val requestId: RequestId = RequestId(BS_IndividualExists_GET)
    individualService.exists(nino).map(_.fold(logAndGenErrorResult, exists => Ok(Json.obj("exists" -> exists))))
  }

  val listOfNinos: Action[Unit] = Action.async(withoutBody) { _ =>
    individualService.listOfNinos.map(listOfNinos => Ok(Json.obj("ninos" -> listOfNinos)))
  }

  val postIndividual: Action[Response[IndividualInRequest]] = Action.async(withJsonBody[IndividualInRequest]) {
    implicit request =>
      implicit val requestId: RequestId = RequestId(BS_Individual_POST)
      request.body.fold(
        logAndSendErrorResult,
        individualService.addIndividual(_).map(_.fold(logAndGenErrorResult, _ => Created))
      )
  }

  val postIndividuals: Action[Response[IndividualsInRequest]] = Action.async(withJsonBody[IndividualsInRequest]) {
    implicit request =>
      implicit val requestId: RequestId = RequestId(BS_Individuals_POST)
      request.body.fold(
        logAndSendErrorResult,
        individualService
          .addIndividuals(_)
          .map(_.fold(logAndGenErrorResult, composeResponse[WriteResult](OK, _)))
      )
  }

  def replaceIndividualDetails(nino: String): Action[Response[IndividualDetails]] =
    Action.async(withJsonBody[IndividualDetails]) { implicit request =>
      implicit val requestId: RequestId = RequestId(BS_Individual_PUT)
      request.body.fold(
        logAndSendErrorResult,
        individualService.replaceIndividualDetails(nino, _).map(_.fold(logAndGenErrorResult, _ => NoContent))
      )
    }

  def retrieveUtr(nino: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    implicit val requestId: RequestId = RequestId(BS_IndividualUtr_GET)
    individualService
      .retrieveUtr(nino)
      .map(
        _.fold(
          logAndGenErrorResult,
          _.fold(NoContent)(utr => Ok(Json.obj("utr" -> utr)))
        )
      )
  }

  def getOverview: Action[Unit] = Action.async(withoutBody) { _ =>
    individualService.listOfPeriodsForAllNinos
      .map(listOfPeriodsByNino => Ok(Json.toJson(PeriodsByNinos(listOfPeriodsByNino))))
  }
}
