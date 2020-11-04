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

package uk.gov.hmrc.breathingspaceifstub.model

import enumeratum._
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json, Writes}

sealed abstract class BaseError(val httpCode: Int, val message: String) extends EnumEntry

object BaseError extends Enum[BaseError] {

  case object BAD_GATEWAY extends BaseError(Status.BAD_GATEWAY, "Downstream systems are not responding")
  case object CONFLICTING_REQUEST extends BaseError(CONFLICT, "The request is conflicting. Maybe a duplicate POST?")
  case object HEADERS_PRECONDITION_NOT_MET extends BaseError(PRECONDITION_REQUIRED, "Invalid header combination")
  case object IDENTIFIER_NOT_FOUND extends BaseError(NOT_FOUND, "The provided identifier cannot be found")
  case object INVALID_BODY extends BaseError(BAD_REQUEST, "Not expected a body to this endpoint")
  case object INVALID_ENDPOINT extends BaseError(BAD_REQUEST, "Not a valid endpoint")
  case object INVALID_FIELDS extends BaseError(BAD_REQUEST, "Invalid query parameter(fields)")
  case object INVALID_IDENTIFIERS extends BaseError(PRECONDITION_REQUIRED, "Invalid path parameter combination")
  case object INVALID_JSON extends BaseError(BAD_REQUEST, "Payload not in the expected Json format")
  case object INVALID_NINO extends BaseError(BAD_REQUEST, "Invalid Nino")
  case object MISSING_BODY extends BaseError(BAD_REQUEST, "The request must have a body")
  case object MISSING_JSON_HEADER extends BaseError(UNSUPPORTED_MEDIA_TYPE, "'Content-Type' header missing or invalid")

  // Only used by test-only endpoints. IDENTIFIER_NOT_FOUND is returned by EIS.
  case object RESOURCE_NOT_FOUND extends BaseError(NOT_FOUND, "The provided identifier cannot be found")

  case object SERVER_ERROR
      extends BaseError(
        INTERNAL_SERVER_ERROR,
        "We are currently experiencing problems that require live service intervention"
      )

  case object SERVICE_UNAVAILABLE extends BaseError(Status.SERVICE_UNAVAILABLE, "Downstream systems are not available")

  case object UNKNOWN_DATA_ITEM
      extends BaseError(UNPROCESSABLE_ENTITY, "1 or more data items in the 'fields' query parameter are incorrect")

  // Must only be used by the ErrorCodeController.
  // The NOT_IMPLEMENTED Http status code was chosen because it's not used by any other BaseError instance.
  case object UNKNOWN_ERROR_CODE extends BaseError(NOT_IMPLEMENTED, "The error code identifier requested is unknown")

  override val values = findValues
}

final case class Failure(baseError: BaseError, detailsToNotShareUpstream: Option[String] = None)

object Failure {

  implicit val writes = new Writes[Failure] {
    def writes(failure: Failure): JsObject =
      Json.obj(
        "code" -> failure.baseError.entryName,
        "reason" -> failure.baseError.message
      )
  }
}
