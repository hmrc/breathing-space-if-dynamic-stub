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

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.INVALID_JSON
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.*
import uk.gov.hmrc.breathingspaceifstub.service.UnderpaymentsService

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class UnderpaymentsController @Inject() (underpaymentsService: UnderpaymentsService, cc: ControllerComponents)(implicit
  val ec: ExecutionContext,
  appConfig: AppConfig
) extends AbstractBaseController(cc, appConfig) {

  def saveUnderpayments(nino: String, periodId: String): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      implicit val requestId: RequestId = RequestId(BS_Underpayments_POST)

      val maybeUnderpayments: Either[String, Underpayments] = request.body.validate[Underpayments] match {
        case JsSuccess(value, _) => Right(value)
        case JsError(errors) => Left(errors.toString)
      }

      val parsingError = maybeUnderpayments match {
        case Left(str) =>
          logger.info(s"Parsing error: " + str)
          true
        case Right(_) =>
          logger.info(s"Controller parsed payload")
          false
      }

      maybeUnderpayments match {
        case Right(underpaymentsWrapper) =>
          underpaymentsService
            .saveUnderpayments(nino, periodId, underpaymentsWrapper.underPayments, logger)
            .map(
              _.fold(
                failure =>
                  if (parsingError) logAndGenFailureResult(Failure(INVALID_JSON))
                  else logAndGenFailureResult(failure),
                bulkWriteResult =>
                  Ok(Json.obj("success" -> s"${bulkWriteResult.successful}", "fails" -> s"${bulkWriteResult.errors}"))
              )
            )
        case Left(error) =>
          logger.error(s"Could not retrieve underpayments: ${error}")
          Future(logAndGenFailureResult(Failure(BAD_REQUEST)))
      }
  }

  def clearUnderpayments: Action[Unit] = Action.async(withoutBody) { implicit request =>
    implicit val requestId: RequestId = RequestId(BS_Underpayments_DELETE)
    underpaymentsService
      .removeUnderpayments()
      .map(
        _.fold(logAndGenErrorResult, count => Ok(Json.obj("deleted" -> count)))
      )
  }

  def get(nino: String, periodId: UUID): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withStaticDataCheck(nino)(staticDataRetrieval(periodId)) { request =>
      withHeaderValidation(BS_Underpayments_GET) { implicit requestId =>
        underpaymentsService
          .get(nino, periodId)
          .map(
            _.fold(
              error => logAndGenFailureResult(error),
              underpayments => if (underpayments.underPayments.isEmpty) NoContent else Ok(Json.toJson(underpayments))
            )
          )
      }
    }
  }

  private def staticDataRetrieval(periodId: UUID)(implicit request: Request[Unit]): String => Option[Result] = nino => {
    def jsonDataFromFile(filename: String): JsValue = getStaticJsonDataFromFile(s"underpayments/$filename")
    (nino.take(8), periodId.toString) match {
      case (n, _) if n.startsWith("BS") => Some(sendErrorResponseFromNino(n)) // a bad nino
      case ("AS000001", "a55d2098-61b3-11ec-9ff0-60f262c313dc") =>
        Some(sendResponse(OK, jsonDataFromFile("underpayments1.json")))
      case ("AS000001", "648ea46e-8027-11ec-b614-03845253624e") =>
        Some(sendResponse(OK, jsonDataFromFile("underpayments1.json")))
      case _ => Some(sendResponse(NOT_FOUND, failures("NO_DATA_FOUND", s"$nino or $periodId did not match")))
    }
  }

  def count(nino: String, periodId: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    implicit val requestId: RequestId = RequestId(BS_Underpayments_GET)
    underpaymentsService
      .count(nino, UUID.fromString(periodId))
      .map(_.fold(logAndGenErrorResult, count => Ok(Json.obj("count" -> count))))
  }
}
