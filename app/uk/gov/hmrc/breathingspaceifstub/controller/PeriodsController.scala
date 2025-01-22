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

import java.util.UUID
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.breathingspaceifstub.Response
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId._
import uk.gov.hmrc.breathingspaceifstub.service.{PeriodsService, UnderpaymentsService}

@Singleton()
class PeriodsController @Inject() (
  periodsService: PeriodsService,
  cc: ControllerComponents,
  underpaymentsService: UnderpaymentsService
)(implicit
  val ec: ExecutionContext
) extends AbstractBaseController(cc) {

  def get(nino: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withHeaderValidation(BS_Periods_GET) { implicit requestId =>
      periodsService
        .get(nino)
        .map(_.fold(logAndGenFailureResult, periods => Ok(Json.toJson(periods))))
    }
  }

  def post(nino: String): Action[Response[PostPeriodsInRequest]] =
    Action.async(withJsonBody[PostPeriodsInRequest]) { implicit request =>
      withHeaderValidation(BS_Periods_POST) { implicit requestId =>
        request.body.fold(
          logAndSendFailureResult,
          periodsService
            .post(nino, _)
            .map(_.fold(logAndGenFailureResult, periods => Created(Json.toJson(periods))))
        )
      }
    }

  def put(nino: String): Action[Response[PutPeriodsInRequest]] =
    Action.async(withJsonBody[PutPeriodsInRequest]) { implicit request =>
      withHeaderValidation(BS_Periods_PUT) { implicit requestId =>
        request.body.fold(
          logAndSendFailureResult,
          periodsService
            .put(nino, _)
            .map(_.fold(logAndGenFailureResult, periods => Ok(Json.toJson(periods))))
        )
      }
    }

  def delete(nino: String, periodId: UUID): Action[Unit] = Action.async(withoutBody) { _ =>
    val fIndividualDel = periodsService.delete(nino, periodId)
    val fUnderpaymentsDel = underpaymentsService.removeUnderpaymentFor(nino, periodId)

    def normalise[A](either: Either[Failure, A]): Int = either match {
      case Right(value) => Integer.parseInt(value.toString)
      case _ => 0
    }

    val fRes = for {
      e1 <- fIndividualDel
      e2 <- fUnderpaymentsDel
    } yield (e1, e2)

    fRes.map { case (e1, e2) =>
      Ok(
        Json.obj(
          "periodsDeleted" -> normalise(e1),
          "underpaymentsDeleted" -> normalise(e2)
        )
      )
    }
  }
}
