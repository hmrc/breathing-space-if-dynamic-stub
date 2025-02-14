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
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.breathingspaceifstub.Header
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.*
import uk.gov.hmrc.breathingspaceifstub.service.MemorandumService

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class MemorandumController @Inject() (
  memorandumService: MemorandumService,
  cc: ControllerComponents,
  appConfig: AppConfig
)(implicit
  val ec: ExecutionContext
) extends AbstractBaseController(cc) {

  private def createResult(httpCode: Int, body: JsValue)(implicit request: Request[_]): Result =
    Status(httpCode)(body)
      .withHeaders(
        Header.CorrelationId -> request.headers
          .get(Header.CorrelationId)
          .getOrElse(UUID.randomUUID().toString)
      )
      .as(MimeTypes.JSON)

  private def failures(code: String, reason: String = "A generic error"): JsValue =
    Json.parse(s"""{"failures":[{"code":"$code","reason":"$reason"}]}""")

  private def jsonBreathingSpaceIndicator(hasIndicator: Boolean) = Json.obj("breathingSpaceIndicator" -> hasIndicator)

  private def mapNinoToResult(nino: String)(implicit request: Request[_]): Option[Result] =
    nino match {
      case "AS000001" => Some(createResult(OK, jsonBreathingSpaceIndicator(hasIndicator = true)))
      case "AS000002" => Some(createResult(OK, jsonBreathingSpaceIndicator(hasIndicator = false)))
      case "AA000333" => Some(createResult(OK, jsonBreathingSpaceIndicator(hasIndicator = true)))
      case "AS000003" => Some(createResult(UNPROCESSABLE_ENTITY, failures("UNKNOWN_DATA_ITEM")))
      case "AS000004" => Some(createResult(BAD_GATEWAY, failures("BAD_GATEWAY")))
      case _ => None
    }

  def get(nino: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withHeaderValidation(BS_Memorandum_GET) { implicit requestId =>
      memorandumService
        .get(nino)
        .map {
          case Left(failure) =>
            if (appConfig.isEnabledStaticData) {
              mapNinoToResult(nino) match {
                case Some(result) => result
                case _ => logAndGenFailureResult(failure)
              }
            } else {
              logAndGenFailureResult(failure)
            }
          case Right(memorandum) => Ok(Json.toJson(memorandum))
        }
    }
  }
}
