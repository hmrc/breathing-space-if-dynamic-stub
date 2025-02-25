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

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_ENDPOINT, UNKNOWN_DATA_ITEM}
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.*
import uk.gov.hmrc.breathingspaceifstub.service.IndividualDetailsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class IndividualDetailsController @Inject() (
  appConfig: AppConfig,
  individualDetailsService: IndividualDetailsService,
  cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractBaseController(cc, appConfig) {

  def get(nino: String, fields: Option[String]): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withStaticDataCheck(nino)(staticDataRetrieval(fields)) { request =>
      fields.fold(fullPopulation(nino))(breathingSpacePopulation(nino, _))
    }
  }

  private def staticDataRetrieval(fields: Option[String])(implicit request: Request[Unit]): String => Option[Result] =
    nino => {
      val fullPopulationDetails = "IndividualDetails.json"
      val detailsForBreathingSpace = "IndividualDetailsForBS.json"

      val filter = {
        val Details = "details(nino,dateOfBirth)"
        val NameList = "nameList(name(firstForename,secondForename,surname,nameType))"
        val AddressList =
          "addressList(address(addressLine1,addressLine2,addressLine3,addressLine4,addressLine5,addressPostcode,countryCode,addressType))"
        val Indicators = "indicators(welshOutputInd)"

        s"$Details,$NameList,$AddressList,$Indicators"
      }
      val result = (nino.toUpperCase.take(8), fields, appConfig.fullPopulationDetailsEnabled) match {
        case (normalisedNino, _, _) if normalisedNino.startsWith("BS") =>
          sendErrorResponseFromNino(normalisedNino) // a bad nino
        case (normalisedNino, None, true) =>
          sendResponseBla(normalisedNino, getStaticDataFromFile(s"individuals/$fullPopulationDetails"))
        case (normalisedNino, None, false) => sendResponse(BAD_REQUEST, failures("INVALID_ENDPOINT"))
        case (normalisedNino, Some(queryString), _) =>
          val qs = queryString.replaceAll("\\s+", "")
          if (qs == filter)
            sendResponseBla(normalisedNino, getStaticDataFromFile(s"individuals/$detailsForBreathingSpace"))
          else sendResponse(UNPROCESSABLE_ENTITY, failures("UNKNOWN_DATA_ITEM"))
      }
      Some(result)
    }

  private def breathingSpacePopulation(nino: String, fields: String)(implicit request: Request[_]): Future[Result] =
    withHeaderValidation(BS_Details_GET) { implicit requestId =>
      fields.replaceAll("\\s+", "") match {
        case IndividualDetailsForBS.fields =>
          individualDetailsService.getIndividualDetailsForBS(nino).map(_.fold(logAndGenFailureResult, Ok(_)))

        case _ => logAndSendFailureResult(Failure(UNKNOWN_DATA_ITEM))
      }
    }

  private def fullPopulation(nino: String)(implicit request: Request[_]): Future[Result] =
    withHeaderValidation(BS_FullDetails_GET) { implicit requestId =>
      if (appConfig.fullPopulationDetailsEnabled) {
        individualDetailsService
          .getIndividualDetails(nino)
          .map(_.fold(logAndGenFailureResult, details => Ok(Json.toJson(details))))
      } else logAndSendFailureResult(Failure(INVALID_ENDPOINT))
    }

}
