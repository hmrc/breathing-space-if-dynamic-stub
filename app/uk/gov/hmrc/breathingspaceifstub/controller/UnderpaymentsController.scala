/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_JSON, NO_DATA_FOUND}
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.service.UnderpaymentsService

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class UnderpaymentsController @Inject()(underpaymentsService: UnderpaymentsService, cc: ControllerComponents)(
  implicit val ec: ExecutionContext
) extends AbstractBaseController(cc) {

  def saveUnderpayments(nino: String, periodId: String): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      implicit val requestId = RequestId(BS_Underpayments_POST)

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
    implicit val requestId = RequestId(BS_Underpayments_DELETE)
    underpaymentsService.removeUnderpayments.map(
      _.fold(logAndGenErrorResult, count => Ok(Json.obj("deleted" -> count)))
    )
  }

  def get(nino: String, periodId: UUID): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withHeaderValidation(BS_Underpayments_GET) { implicit requestId =>
      underpaymentsService
        .get(nino, periodId)
        .map(
          _.fold(
            failure => {
              if (failure.baseError != BaseError.GATEWAY_TIMEOUT) {
                logAndGenFailureResult(Failure(NO_DATA_FOUND))
              } else {
                logAndGenFailureResult(failure)
              }
            },
            underpayments => Ok(Json.toJson(underpayments))
          )
        )
    }
  }

  def count(nino: String, periodId: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    implicit val requestId = RequestId(BS_Underpayments_GET)
    underpaymentsService
      .count(nino, UUID.fromString(periodId))
      .map(_.fold(logAndGenErrorResult, count => Ok(Json.obj("count" -> count))))
  }
}
