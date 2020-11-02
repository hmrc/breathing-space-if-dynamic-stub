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

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.breathingspaceifstub.Response
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId._
import uk.gov.hmrc.breathingspaceifstub.service.PeriodsService

@Singleton()
class PeriodsController @Inject()(periodsService: PeriodsService, cc: ControllerComponents)(
  implicit val ec: ExecutionContext
) extends AbstractBaseController(cc) {

  def post(nino: String): Action[Response[PostPeriodsInRequest]] =
    Action.async(withJsonBody[PostPeriodsInRequest]) { implicit request =>
      implicit val requestId = RequestId(BS_Periods_POST)
      request.body.fold(
        logAndSendErrorResult,
        periodsService
          .post(nino, _)
          .map(_.fold(logAndGenErrorResult, periods => Created(Json.toJson(periods))))
      )
    }
}