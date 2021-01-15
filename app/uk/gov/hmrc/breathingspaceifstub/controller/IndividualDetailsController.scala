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

package uk.gov.hmrc.breathingspaceifstub.controller

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_ENDPOINT, UNKNOWN_DATA_ITEM}
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId._
import uk.gov.hmrc.breathingspaceifstub.service.IndividualDetailsService

@Singleton()
class IndividualDetailsController @Inject()(
  appConfig: AppConfig,
  individualDetailsService: IndividualDetailsService,
  cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractBaseController(cc) {

  def get(nino: String, fields: Option[String]): Action[Unit] = Action.async(withoutBody) { implicit request =>
    fields.fold(fullPopulation(nino))(breathingSpacePopulation(nino, _))
  }

  private def breathingSpacePopulation(nino: String, fields: String)(implicit request: Request[_]): Future[Result] =
    fields.replaceAll("\\s+", "") match {
      case IndividualDetailsForBS.fields =>
        implicit val requestId = RequestId(BS_Details_GET)
        individualDetailsService
          .getIndividualDetailsForBS(nino)
          .map(_.fold(logAndGenFailureResult, Ok(_)))

      case _ =>
        implicit val requestId = RequestId(BS_Details_GET)
        logAndSendFailureResult(Failure(UNKNOWN_DATA_ITEM))
    }

  private def fullPopulation(nino: String)(implicit request: Request[_]): Future[Result] = {
    implicit val requestId = RequestId(BS_FullDetails_GET)
    if (appConfig.fullPopulationDetailsEnabled) {
      individualDetailsService
        .getIndividualDetails(nino)
        .map(_.fold(logAndGenFailureResult, details => Ok(Json.toJson(details))))
    } else logAndSendFailureResult(Failure(INVALID_ENDPOINT))
  }
}
