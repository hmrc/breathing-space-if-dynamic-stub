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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.NO_DATA_FOUND
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.*
import uk.gov.hmrc.breathingspaceifstub.service.DebtsService

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class DebtsController @Inject() (debtsService: DebtsService, cc: ControllerComponents)(implicit
  val ec: ExecutionContext,
  appConfig: AppConfig
) extends AbstractBaseController(cc, appConfig) {

  def get(nino: String, periodId: UUID): Action[Unit] = Action.async(withoutBody) { implicit request =>

    /*
  def getAcceptedNinoHandler(nino: String)(implicit request: Request[_]): Future[Result] =
    nino match {
      case "AS000001" => sendResponse(OK, jsonDataFromFile("singleBsDebtFullPopulation.json"))
      case "AS000002" => sendResponse(OK, jsonDataFromFile("singleBsDebtPartialPopulation.json"))
      case "AS000003" => sendResponse(OK, jsonDataFromFile("multipleBsDebtsFullPopulation.json"))
      case "AS000004" => sendResponse(OK, jsonDataFromFile("multipleBsDebtsPartialPopulation.json"))
      case "AS000005" => sendResponse(OK, jsonDataFromFile("multipleBsDebtsMixedPopulation.json"))
      case _          => sendResponse(NOT_FOUND, failures("NO_DATA_FOUND", "No records found for the given Nino"))
    }


     */

    val staticRetrieval: String => Option[Result] = nino => {
      def jsonDataFromFile(filename: String): JsValue = getStaticJsonDataFromFile(s"debts/$filename")
      nino.take(8) match {
        case "AS000001" => Some(sendResponse(OK, jsonDataFromFile("singleBsDebtFullPopulation.json")))
        case "AS000002" => Some(sendResponse(OK, jsonDataFromFile("singleBsDebtPartialPopulation.json")))
        case "AS000003" => Some(sendResponse(OK, jsonDataFromFile("multipleBsDebtsFullPopulation.json")))
        case "AS000004" => Some(sendResponse(OK, jsonDataFromFile("multipleBsDebtsPartialPopulation.json")))
        case "AS000005" => Some(sendResponse(OK, jsonDataFromFile("multipleBsDebtsMixedPopulation.json")))
        case n if n.startsWith("BS") => Some(sendErrorResponseFromNino(n)) // a bad nino
        case _ => Some(sendResponse(NOT_FOUND, failures("NO_DATA_FOUND", "No records found for the given Nino")))
        //    case _ => None
      }
    }

    withStaticCheck(nino)(staticRetrieval) { request =>
      withHeaderValidation(BS_Debts_GET) { implicit requestId =>
        debtsService
          .get(nino, periodId)
          .map(
            _.fold(
              logAndGenFailureResult,
              debts =>
                if (debts.isEmpty) logAndGenFailureResult(Failure(NO_DATA_FOUND))
                else Ok(Json.toJson(debts))
            )
          )
      }
    }
  }
}
