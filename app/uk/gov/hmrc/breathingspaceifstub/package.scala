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
    lazy val CorrelationId = "CorrelationId"
    lazy val OriginatorId = "OriginatorId"
    lazy val UserId = "UserId"
  }

  lazy val httpErrorMap = Map[Int, String](
    400 -> "BAD_REQUEST",
    401 -> "UNAUTHORIZED",
    402 -> "PAYMENT_REQUIRED",
    403 -> "FORBIDDEN",
    404 -> "RESOURCE_NOT_FOUND",
    405 -> "METHOD_NOT_ALLOWED",
    406 -> "NOT_ACCEPTABLE",
    407 -> "PROXY_AUTHENTICATION_REQUIRED",
    408 -> "REQUEST_TIMEOUT",
    409 -> "CONFLICT",
    410 -> "GONE",
    411 -> "LENGTH_REQUIRED",
    412 -> "PRECONDITION_FAILED",
    413 -> "REQUEST_ENTITY_TOO_LARGE",
    414 -> "REQUEST_URI_TOO_LONG",
    415 -> "UNSUPPORTED_MEDIA_TYPE",
    416 -> "REQUESTED_RANGE_NOT_SATISFIABLE",
    417 -> "EXPECTATION_FAILED",
    422 -> "UNPROCESSABLE_ENTITY",
    423 -> "LOCKED",
    424 -> "FAILED_DEPENDENCY",
    426 -> "UPGRADE_REQUIRED",
    428 -> "PRECONDITION_REQUIRED",
    429 -> "TOO_MANY_REQUESTS",
    500 -> "INTERNAL_SERVER_ERROR",
    501 -> "NOT_IMPLEMENTED",
    502 -> "BAD_GATEWAY",
    503 -> "SERVICE_UNAVAILABLE",
    504 -> "GATEWAY_TIMEOUT",
    505 -> "HTTP_VERSION_NOT_SUPPORTED",
    507 -> "INSUFFICIENT_STORAGE",
    511 -> "NETWORK_AUTHENTICATION_REQUIRED"
  )

  private val prefix = "BS"

  lazy val httpErrorSet = Set[String](
    elems = s"${prefix}000400",
    s"${prefix}000401",
    s"${prefix}000402",
    s"${prefix}000403",
    s"${prefix}000404",
    s"${prefix}000405",
    s"${prefix}000406",
    s"${prefix}000407",
    s"${prefix}000408",
    s"${prefix}000409",
    s"${prefix}000410",
    s"${prefix}000411",
    s"${prefix}000412",
    s"${prefix}000413",
    s"${prefix}000414",
    s"${prefix}000415",
    s"${prefix}000416",
    s"${prefix}000417",
    s"${prefix}000422",
    s"${prefix}000423",
    s"${prefix}000424",
    s"${prefix}000426",
    s"${prefix}000428",
    s"${prefix}000429",
    s"${prefix}000500",
    s"${prefix}000501",
    s"${prefix}000502",
    s"${prefix}000503",
    s"${prefix}000504",
    s"${prefix}000505",
    s"${prefix}000507",
    s"${prefix}000511"
  )
}
