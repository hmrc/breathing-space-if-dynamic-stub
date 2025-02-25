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

import play.api.libs.json.*
import play.api.libs.json.Reads.*
import play.api.mvc.*
import uk.gov.hmrc.breathingspaceifstub.Response
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.model.*
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{INVALID_JSON, MISSING_BODY}
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId.*
import uk.gov.hmrc.breathingspaceifstub.service.{PeriodsService, UnderpaymentsService}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class PeriodsController @Inject() (
  periodsService: PeriodsService,
  cc: ControllerComponents,
  underpaymentsService: UnderpaymentsService,
  appConfig: AppConfig
)(implicit
  val ec: ExecutionContext
) extends AbstractBaseController(cc, appConfig) {

  def get(nino: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    withStaticDataCheck(nino)(staticDataRetrieval) { request =>
      withHeaderValidation(BS_Periods_GET) { implicit requestId =>
        periodsService
          .get(nino)
          .map(_.fold(logAndGenFailureResult, periods => Ok(Json.toJson(periods))))
      }
    }
  }

  private def staticDataRetrieval(implicit request: Request[Unit]): String => Option[Result] = nino => {
    def jsonDataFromFile(filename: String): JsValue = getStaticJsonDataFromFile(s"periods/$filename")
    nino.take(8) match {
      case "AS000001" => Some(sendResponse(OK, jsonDataFromFile("singleBsPeriodFullPopulation.json")))
      case "AS000002" => Some(sendResponse(OK, jsonDataFromFile("singleBsPeriodPartialPopulation.json")))
      case "AS000003" => Some(sendResponse(OK, jsonDataFromFile("multipleBsPeriodsFullPopulation.json")))
      case "AS000004" => Some(sendResponse(OK, jsonDataFromFile("multipleBsPeriodsPartialPopulation.json")))
      case "AS000005" => Some(sendResponse(OK, jsonDataFromFile("multipleBsPeriodsMixedPopulation.json")))
      case n if n.startsWith("BS") => Some(sendErrorResponseFromNino(n)) // a bad nino
      case _ => Some(sendResponse(OK, Json.parse("""{"periods":[]}""")))
    }
  }

  private def staticDataRetrievalForPOSTAndPUT[A](
    httpSuccessCode: Int,
    addPeriodIdField: Boolean
  )(implicit request: Request[Response[A]], writes: Writes[A]): String => Option[Result] = nino =>
    (nino, request.body) match {
      case (n, _) if n.startsWith("BS") => Some(sendErrorResponseFromNino(n)) // a bad nino
      case (n, Left(f)) if f.baseError == MISSING_BODY =>
        Some(sendResponse(BAD_REQUEST, failures("MISSING_BODY", "The request must have a body")))
      case (n, Left(f)) if f.baseError == INVALID_JSON =>
        Some(sendResponse(BAD_REQUEST, failures("INVALID_JSON", "Payload not in the expected Json format")))
      case (n, Right(p)) =>
        def transformRequestJsonToResponseJson(jsValue: JsValue, addPeriodIdField: Boolean): JsResult[JsObject] = {
          val attrTransformer = (__ \ "periods").json.update {
            __.read[JsArray].map { case JsArray(values) =>
              val updatedValues = values.map { period =>
                val retainedFields = period.as[JsObject].fields.filter(_._1 != "pegaRequestTimestamp")
                val additionalFields =
                  if (addPeriodIdField) Seq(("periodID", JsString(UUID.randomUUID().toString))) else Seq.empty
                JsObject(additionalFields ++ retainedFields)
              }

              JsArray(updatedValues)
            }
          }
          jsValue.transform(attrTransformer)
        }

        val jsValue = Json.toJson(p)
        transformRequestJsonToResponseJson(jsValue, addPeriodIdField) match {
          case JsError(_) =>
            Some(
              sendResponse(
                BAD_REQUEST,
                failures("INVALID_JSON", "Payload not in the expected Json format")
              )
            )

          case JsSuccess(jsObject, _) => Some(sendResponse(httpSuccessCode, jsObject))
        }

      case _ => Some(sendResponse(OK, Json.parse("""{"periods":[]}""")))
    }

  def post(nino: String): Action[Response[PostPeriodsInRequest]] =
    Action.async(withJsonBody[PostPeriodsInRequest]) { implicit request: Request[Response[PostPeriodsInRequest]] =>
      withStaticDataCheck[Response[PostPeriodsInRequest]](nino)(
        staticDataRetrievalForPOSTAndPUT(CREATED, addPeriodIdField = true)
      ) { request =>
        withHeaderValidation(BS_Periods_POST) { implicit requestId =>
          request.body.fold(
            logAndSendFailureResult,
            periodsService
              .post(nino, _)
              .map(_.fold(logAndGenFailureResult, periods => Created(Json.toJson(periods))))
          )
        }
      }
    }

  def put(nino: String): Action[Response[PutPeriodsInRequest]] =
    Action.async(withJsonBody[PutPeriodsInRequest]) { implicit request =>
      withStaticDataCheck[Response[PutPeriodsInRequest]](nino)(
        staticDataRetrievalForPOSTAndPUT(CREATED, addPeriodIdField = true)
      ) {
        withHeaderValidation(BS_Periods_PUT) { implicit requestId =>
          request.body.fold(
            logAndSendFailureResult,
            periodsService
              .put(nino, _)
              .map(_.fold(logAndGenFailureResult, periods => Ok(Json.toJson(periods))))
          )
        }
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
