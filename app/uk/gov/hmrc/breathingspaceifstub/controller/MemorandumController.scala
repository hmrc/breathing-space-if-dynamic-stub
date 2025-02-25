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

import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.*
import uk.gov.hmrc.breathingspaceifstub.service.MemorandumService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class MemorandumController @Inject() (
  memorandumService: MemorandumService,
  cc: ControllerComponents,
  appConfig: AppConfig
)(implicit
  val ec: ExecutionContext
) extends AbstractBaseController(cc, appConfig) {

  def get(nino: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withStaticCheck(nino)(staticRetrieval) {
      withHeaderValidation(BS_Memorandum_GET) { implicit requestId =>
        memorandumService
          .get(nino)
          .map {
            case Left(failure) =>
              logAndGenFailureResult(failure)
            case Right(memorandum) =>
              Ok(Json.toJson(memorandum))
          }
      }
    }
  }

  private def staticRetrieval(implicit request: Request[Unit]): String => Option[Result] = nino => {
    def jsonBreathingSpaceIndicator(hasIndicator: Boolean) = Json.obj("breathingSpaceIndicator" -> hasIndicator)
    nino.take(8) match {
      case "AS000001" => Some(createResult(OK, jsonBreathingSpaceIndicator(hasIndicator = true)))
      case "AS000002" => Some(createResult(OK, jsonBreathingSpaceIndicator(hasIndicator = false)))
      case "AA000333" => Some(createResult(OK, jsonBreathingSpaceIndicator(hasIndicator = true)))
      case "AS000003" => Some(createResult(UNPROCESSABLE_ENTITY, failures("UNKNOWN_DATA_ITEM")))
      case "AS000004" => Some(createResult(BAD_GATEWAY, failures("BAD_GATEWAY")))
      case _ => None
    }
  }

}
