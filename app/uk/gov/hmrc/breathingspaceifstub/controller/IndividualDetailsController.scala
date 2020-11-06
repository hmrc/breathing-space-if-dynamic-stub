/*
 * Copyright 2020 HM Revenue & Customs
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

import scala.concurrent.ExecutionContext

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.breathingspaceifstub.model.{Failure, IndividualDetail0, RequestId}
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.UNKNOWN_DATA_ITEM
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId._
import uk.gov.hmrc.breathingspaceifstub.schema.IndividualDetail1
import uk.gov.hmrc.breathingspaceifstub.service.IndividualDetailsService

@Singleton()
class IndividualDetailsController @Inject()(
  individualDetailsService: IndividualDetailsService,
  cc: ControllerComponents
)(
  implicit val ec: ExecutionContext
) extends AbstractBaseController(cc) {

  def get(nino: String, fields: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    fields.replaceAll("\\s+", "") match {
      case IndividualDetail0.fields =>
        implicit val requestId = RequestId(BS_Detail0_GET)
        individualDetailsService
          .getIndividualDetail0(nino)
          .map(_.fold(logAndGenErrorResult, individualDetail0 => Ok(Json.toJson(individualDetail0))))

      case IndividualDetail1.fields =>
        implicit val requestId = RequestId(BS_Detail1_GET)
        individualDetailsService
          .getIndividualDetail1(nino)
          .map(_.fold(logAndGenErrorResult, individualDetail1 => Ok(Json.toJson(individualDetail1))))

      case "" =>
        implicit val requestId = RequestId(BS_Details_GET)
        individualDetailsService
          .getIndividualDetails(nino)
          .map(
            _.fold(
              logAndGenErrorResult,
              individualDetails =>
                Ok(Json.obj("nino" -> Json.toJson(nino)) ++ Json.toJson(individualDetails).as[JsObject])
            )
          )

      case _ =>
        implicit val requestId = RequestId(BS_Detail_GET)
        logAndGenHttpError(Failure(UNKNOWN_DATA_ITEM)).send
    }
  }
}
