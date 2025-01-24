/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import uk.gov.hmrc.breathingspaceifstub.model.{Underpayment, Underpayments}

class UnderpaymentsSerializationSpec extends AnyWordSpec with Matchers {

  val up1: Underpayment = Underpayment("2011", 123423.222, "SA UP")
  val up2: Underpayment = Underpayment("2011", 123423.222, "SA UP")
  val up3: Underpayment = Underpayment("2011", 123423.222, "SA UP")

  val underpayments: List[Underpayment] = List(up1, up2, up3)

  val json = "{\"taxYear\":\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"}"

  val jsonList: String = "[{\"taxYear\":\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"}," +
    "{\"taxYear\":\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"}," +
    "{\"taxYear\":\"2011\",\"amount\":123423.222,\"source\":\"SA UP\"}]"

  val wrapper: Underpayments = Underpayments(underpayments)

  "JSON serialization" should {
    "serialize a list of underpayments to JSON" in {
      val serializedToJson: JsValue = Json.toJson(underpayments)
      val minifiedString = Json.stringify(serializedToJson)
      minifiedString shouldBe jsonList
    }

    "deserialize JSON to an underpayment" in {
      val serializedUnderpayment: JsValue = Json.parse(json)
      val maybeUnderpayment: JsResult[Underpayment] = serializedUnderpayment.validate[Underpayment]

      val u: Underpayment = maybeUnderpayment match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw new RuntimeException("Parse error: " + errors)
      }

      u shouldBe up1
    }

    "deserialize empty underpayments array" in {
      val emptyUPs: JsValue = Json.parse("{\"underPayments\":[]}")

      val maybeUPs = emptyUPs.validate[Underpayments]
      val actualUnderpayments = maybeUPs match {
        case JsSuccess(x, _) => x
        case JsError(errors) => throw new RuntimeException("Failed: " + errors)
      }

      val emptyUnderpayments = Array.empty[Underpayment] // Specify type explicitly
      actualUnderpayments.underPayments shouldBe emptyUnderpayments.toList // Convert to List for comparison
    }

    "serialize an empty underpayments array" in {
      val emptyWrapper = Underpayments(List.empty)
      val serializedWrapper: JsValue = Json.toJson(emptyWrapper)
      val expectedOutput = "{\"underPayments\":[]}"

      val actualOutput = Json.stringify(serializedWrapper)

      expectedOutput shouldBe actualOutput
    }
  }
}
