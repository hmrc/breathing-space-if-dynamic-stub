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

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_ENDPOINT, UNKNOWN_DATA_ITEM}
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.*
import uk.gov.hmrc.breathingspaceifstub.service.IndividualDetailsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton()
class IndividualDetailsController @Inject() (
  appConfig: AppConfig,
  individualDetailsService: IndividualDetailsService,
  cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractBaseController(cc, appConfig) {

  def get(nino: String, fields: Option[String]): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withStaticCheck(nino)(nino => Some(composeResponseFromNino(nino, getAcceptedNinoHandler(fields)))) { request =>
      fields.fold(fullPopulation(nino))(breathingSpacePopulation(nino, _))
    }
  }

  private val fullPopulationDetails = "IndividualDetails.json"
  private val detailsForBreathingSpace = "IndividualDetailsForBS.json"

  private val filter = {
    val Details = "details(nino,dateOfBirth)"
    val NameList = "nameList(name(firstForename,secondForename,surname,nameType))"
    val AddressList =
      "addressList(address(addressLine1,addressLine2,addressLine3,addressLine4,addressLine5,addressPostcode,countryCode,addressType))"
    val Indicators = "indicators(welshOutputInd)"

    s"$Details,$NameList,$AddressList,$Indicators"
  }

  private val httpErrorCodes = Map(
    400 -> "BAD_REQUEST",
    401 -> "UNAUTHORIZED",
    402 -> "PAYMENT_REQUIRED",
    403 -> "BREATHINGSPACE_EXPIRED",
    404 -> "RESOURCE_NOT_FOUND",
    405 -> "METHOD_NOT_ALLOWED",
    406 -> "NOT_ACCEPTABLE",
    407 -> "PROXY_AUTHENTICATION_REQUIRED",
    408 -> "REQUEST_TIMEOUT",
    409 -> "CONFLICTING_REQUEST",
    410 -> "GONE",
    411 -> "LENGTH_REQUIRED",
    412 -> "PRECONDITION_FAILED",
    413 -> "REQUEST_ENTITY_TOO_LARGE",
    414 -> "REQUEST_URI_TOO_LONG",
    415 -> "MISSING_JSON_HEADER",
    416 -> "REQUESTED_RANGE_NOT_SATISFIABLE",
    417 -> "EXPECTATION_FAILED",
    422 -> "UNKNOWN_DATA_ITEM",
    423 -> "LOCKED",
    424 -> "FAILED_DEPENDENCY",
    426 -> "UPGRADE_REQUIRED",
    428 -> "HEADERS_PRECONDITION_NOT_MET",
    429 -> "TOO_MANY_REQUESTS",
    500 -> "SERVER_ERROR",
    501 -> "NOT_IMPLEMENTED",
    502 -> "BAD_GATEWAY",
    503 -> "SERVICE_UNAVAILABLE",
    504 -> "GATEWAY_TIMEOUT",
    505 -> "HTTP_VERSION_NOT_SUPPORTED",
    507 -> "INSUFFICIENT_STORAGE",
    511 -> "NETWORK_AUTHENTICATION_REQUIRED"
  )

  private def getAcceptedNinoHandler(
    fields: Option[String]
  )(nino: String)(implicit request: Request[_]): Result =
    fields.fold {
      if (appConfig.fullPopulationDetailsEnabled)
        sendResponseBla(nino, getDataFromFile(s"individuals/$fullPopulationDetails"))
      else sendResponse(BAD_REQUEST, failures("INVALID_ENDPOINT"))
    } { queryString =>
      val qs = queryString.replaceAll("\\s+", "")
      if (qs == filter) sendResponseBla(nino, getDataFromFile(s"individuals/$detailsForBreathingSpace"))
      else sendResponse(UNPROCESSABLE_ENTITY, failures("UNKNOWN_DATA_ITEM"))
    }

  private def sendErrorResponseFromNino(nino: String)(implicit request: Request[_]): Result = {
    val statusCode = Try(nino.substring(5, 8).toInt).getOrElse(INTERNAL_SERVER_ERROR)
    httpErrorCodes
      .get(statusCode)
      .fold(sendResponse(INTERNAL_SERVER_ERROR, failures("SERVER_ERROR"))) { code =>
        sendResponse(statusCode, failures(code))
      }
  }

  def composeResponseFromNino(nino: String, acceptedHandler: (String) => Result)(implicit
    request: Request[_]
  ): Result = {
    val normalisedNino = nino.toUpperCase.take(8)
    if (normalisedNino.take(2) == "BS") sendErrorResponseFromNino(normalisedNino) // a bad nino
    else acceptedHandler(normalisedNino)
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
