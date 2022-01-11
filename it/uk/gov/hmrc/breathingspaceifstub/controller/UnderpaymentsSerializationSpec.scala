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

package uk.gov.hmrc.breathingspaceifstub.controller

import uk.gov.hmrc.breathingspaceifstub.model.{Underpayment, Underpayments}
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class UnderpaymentsSerializationSpec extends BaseISpec {

  import play.api.libs.json._

  val u1 = Underpayment("2011", 123423.222, "SA UP")
  val u2 = Underpayment("2011", 123423.222, "SA UP")
  val u3 = Underpayment("2011", 123423.222, "SA UP")

  val underpayments = List(u1, u2, u3)

  val json = "{\"taxYear\":\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"}"
  val jsonList = "[{\"taxYear\":\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"}" +
    ",{\"taxYear\":\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"},{\"taxYear\":" +
    "\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"}]"

  val wrapper = Underpayments(underpayments)

  test("json tests - serialization") {
    val serializedToJson: JsValue = Json.toJson(underpayments)
    val minifiedString = Json.stringify(serializedToJson) // Json.prettyPrint(serializedToJson)
    minifiedString shouldBe jsonList
  }

  test("json tests - deserialization") {
    val serializedUnderpayment: JsValue = Json.parse(json)
    val maybeUnderpayment: JsResult[Underpayment] = serializedUnderpayment.validate[Underpayment]
    val u: Underpayment = maybeUnderpayment match {
      case JsSuccess(value, _) => value
      case _ => throw new RuntimeException("parse error")
    }
    u shouldBe u1
  }

  test("json tests - deserialize empty underpayments array") {
    val emptyUPs: JsValue = Json.parse("{\"underPayments\":[]}")

    val maybeUPs = emptyUPs.validate[Underpayments]
    val actualUnderpayments = maybeUPs match {
      case JsSuccess(x, _) => x
      case JsError(errors) => throw new RuntimeException("Failed: " + errors)
    }

    val emptyUnderpayments = Array.empty
    actualUnderpayments.underPayments should equal(emptyUnderpayments)
  }

  test("json tests - serialize empty underpayments array") {
    val emptyWrapper = Underpayments(List.empty)
    val serializedWrapper: JsValue = Json.toJson(emptyWrapper)
    val expectedOutput = "{\"underPayments\":[]}"

    val actualOutput = Json.stringify(serializedWrapper)

    expectedOutput should equal(actualOutput)
  }
}
