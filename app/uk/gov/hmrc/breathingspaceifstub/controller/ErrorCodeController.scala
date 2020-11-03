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

import cats.syntax.option._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.breathingspaceifstub.model.{BaseError, Failure, HttpError}
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.UNKNOWN_ERROR_CODE

@Singleton()
class ErrorCodeController @Inject()(cc: ControllerComponents)(implicit val ec: ExecutionContext)
    extends AbstractBaseController(cc) {

  def get(baseError: String): Action[Unit] = Action.async(withoutBody) { _ =>
    val failure = BaseError.values
      .find(_.entryName == baseError)
      .fold(Failure(UNKNOWN_ERROR_CODE, s"($baseError)".some))(Failure(_))

    HttpError(none, failure).send
  }
}
