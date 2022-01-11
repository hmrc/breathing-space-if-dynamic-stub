/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.breathingspaceifstub.httpErrorMap

sealed abstract class BaseError(val httpCode: Int, val message: String) extends EnumEntry

object BaseError extends Enum[BaseError] {

  case object BAD_GATEWAY extends BaseError(Status.BAD_GATEWAY, "Downstream systems are not responding")

  case object BREATHINGSPACE_EXPIRED extends BaseError(FORBIDDEN, "Breathing Space has expired for the given Nino")

  case object BREATHINGSPACE_ID_NOT_FOUND
      extends BaseError(NOT_FOUND, "The provided Breathing Space Period reference was not found")

  case object CONFLICTING_REQUEST extends BaseError(CONFLICT, "The request is conflicting. Maybe a duplicate POST?")

  case object DUPLICATE_SUBMISSION
      extends BaseError(CONFLICT, "The Breathing Space Period(s) being created already exists")

  case object HEADERS_PRECONDITION_NOT_MET extends BaseError(PRECONDITION_REQUIRED, "Invalid header combination")
  case object IDENTIFIER_NOT_FOUND extends BaseError(NOT_FOUND, "The provided identifier cannot be found")
  case object IDENTIFIER_NOT_IN_BREATHINGSPACE extends BaseError(NOT_FOUND, "The given Nino is not in Breathing Space")
  case object INVALID_BODY extends BaseError(BAD_REQUEST, "Not expected a body to this endpoint")
  case object INVALID_ENDPOINT extends BaseError(BAD_REQUEST, "Not a valid endpoint")
  case object INVALID_HEADER extends BaseError(BAD_REQUEST, "Invalid value for the header")
  case object INVALID_IDENTIFIERS extends BaseError(PRECONDITION_REQUIRED, "Invalid path parameter combination")
  case object INVALID_JSON extends BaseError(BAD_REQUEST, "Payload not in the expected Json format")
  case object INVALID_NINO extends BaseError(BAD_REQUEST, "Invalid Nino")
  case object MISSING_BODY extends BaseError(BAD_REQUEST, "The request must have a body")
  case object MISSING_HEADER extends BaseError(BAD_REQUEST, "Missing required header")
  case object MISSING_JSON_HEADER extends BaseError(UNSUPPORTED_MEDIA_TYPE, "'Content-Type' header missing or invalid")
  case object NO_DATA_FOUND extends BaseError(NOT_FOUND, "No records found for the given Nino")
  case object INVALID_UNDERPAYMENT extends BaseError(BAD_REQUEST, "Invalid underpayment")

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

  override val values = findValues
}

final case class Failure(baseError: BaseError, detailsToNotShareUpstream: Option[String] = None)

object Failure {

  class HttpErrorCode(httpCode: Int, message: String) extends BaseError(httpCode = httpCode, message = message)

  def apply(httpCode: Int): Failure =
    Failure(new HttpErrorCode(httpCode, "Nino suffixed with Http error code"))

  implicit val writes = new Writes[Failure] {
    def writes(failure: Failure): JsObject = {
      val bE = failure.baseError
      val code =
        if (bE.isInstanceOf[HttpErrorCode]) httpErrorMap.getOrElse(bE.httpCode, bE.httpCode.toString)
        else bE.entryName

      Json.obj("code" -> code, "reason" -> bE.message)
    }
  }

  val asErrorItem = new Writes[Failure] {
    def writes(failure: Failure): JsObject =
      Json.obj(
        "code" -> failure.baseError.entryName,
        "message" -> failure.baseError.message
      )
  }
}
