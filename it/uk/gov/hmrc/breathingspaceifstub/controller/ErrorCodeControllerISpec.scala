package uk.gov.hmrc.breathingspaceifstub.controller

import play.api.http.Status.NOT_IMPLEMENTED
import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{MISSING_JSON_HEADER, UNKNOWN_ERROR_CODE}
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class ErrorCodeControllerISpec extends BaseISpec {

  test("\"get\" should return an error message according to the provided BaseError code") {
    val code = MISSING_JSON_HEADER.entryName
    val response = baseError(code)
    status(response) shouldBe UNSUPPORTED_MEDIA_TYPE
    (contentAsJson(response) \ "failures" \\ "code").head.as[String] shouldBe code
  }

  test("\"get\" should return 501(NOT_IMPLEMENTED) when receiving an unknown BaseError code") {
    val response = baseError("WHATEVER")
    status(response) shouldBe NOT_IMPLEMENTED
    (contentAsJson(response) \ "failures" \\ "code").head.as[String] shouldBe UNKNOWN_ERROR_CODE.entryName
  }
}
