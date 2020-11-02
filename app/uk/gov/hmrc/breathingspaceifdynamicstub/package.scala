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

package uk.gov.hmrc

import scala.concurrent.Future

import uk.gov.hmrc.breathingspaceifstub.model.Failure

package object breathingspaceifstub {

  val unit: Unit = ()

  type Response[T] = Either[Failure, T]
  type AsyncResponse[T] = Future[Response[T]]

  object Header {
    lazy val Authorization = "Authorization"
    lazy val Environment = "Environment"
    lazy val CorrelationId = "Correlation-Id"
    lazy val OriginatorId = "OriginatorId"
    lazy val UserId = "UserId"
  }
}
