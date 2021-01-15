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

import scala.util.{Failure, Success}

import controllers.Assets
import play.api.Logging
import play.api.http.MimeTypes
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.breathingspaceifstub.config.AppConfig
import uk.gov.hmrc.breathingspaceifstub.views.txt
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.ramltools.loaders.UrlRamlLoader

@Singleton
class DocumentationController @Inject()(
  appConfig: AppConfig,
  ramlLoader: UrlRamlLoader,
  cc: ControllerComponents,
  assets: Assets
) extends BackendController(cc)
    with Logging {

  val definition: Action[AnyContent] = Action {
    logger.debug(s"DocumentationController definition endpoint has been called")
    Ok(txt.definition(appConfig.v1WhitelistedApplicationIds)).as(MimeTypes.JSON)
  }

  def raml(version: String, file: String): Action[AnyContent] =
    assets.at(s"/api/conf/$version", file)

  def verify(ramlUrl: String): Action[AnyContent] = Action {
    ramlLoader.load(ramlUrl) match {
      case Success(_) => Ok
      case Failure(error) => UnprocessableEntity(error.getMessage)
    }
  }
}
