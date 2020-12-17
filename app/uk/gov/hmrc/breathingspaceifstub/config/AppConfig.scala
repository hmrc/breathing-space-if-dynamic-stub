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

package uk.gov.hmrc.breathingspaceifstub.config

import javax.inject.{Inject, Singleton}

import play.api.Configuration

final case class HeaderMapping(nameToMap: String, nameMapped: String)

@Singleton
class AppConfig @Inject()(config: Configuration) {

  lazy val fullPopulationDetailsEnabled: Boolean =
    config.getOptional[Boolean]("full-population-details-enabled").getOrElse(false)

  lazy val onDevEnvironment: Boolean =
    config.getOptional[String]("environment.id").fold(false)(_.toLowerCase == "development")

  // Must be 'lazy'
  lazy val v1WhitelistedApplicationIds =
    config.get[Seq[String]]("api.access.version-1.0.whitelistedApplicationIds")
}
