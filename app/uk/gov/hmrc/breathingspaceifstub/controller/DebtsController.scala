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
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.NO_DATA_FOUND
import uk.gov.hmrc.breathingspaceifstub.model.EndpointId._
import uk.gov.hmrc.breathingspaceifstub.service.DebtsService

@Singleton()
class DebtsController @Inject()(debtsService: DebtsService, cc: ControllerComponents)(
  implicit val ec: ExecutionContext
) extends AbstractBaseController(cc) {

  def get(nino: String): Action[Unit] = Action.async(withoutBody) { implicit request =>
    implicit val requestId = RequestId(BS_Debts_GET)
    debtsService
      .get(nino)
      .map(
        _.fold(
          logAndGenFailureResult,
          debts =>
            if (debts.isEmpty) logAndGenFailureResult(Failure(NO_DATA_FOUND))
            else Ok(Json.toJson(debts))
        )
      )
  }
}
