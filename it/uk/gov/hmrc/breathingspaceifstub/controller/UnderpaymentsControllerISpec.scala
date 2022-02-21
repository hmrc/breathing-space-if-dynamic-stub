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

import play.api.http.Status.{CONFLICT, OK}
import play.api.test.Helpers.{contentAsJson, contentAsString, status}
import uk.gov.hmrc.breathingspaceifstub.model.Underpayments
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

import java.util.UUID

class UnderpaymentsControllerISpec extends BaseISpec {

  test("\"POST\" Underpayments should not allow duplicates") {

//    pending

    // create some underpayments
    val n1 = "AS000001A"
    val p1 = "1519948e-8a54-11ec-8ed1-5bb13a0b0e94"
    status(postUnderpayments(n1, p1, Underpayments(List(u1)))) shouldBe OK

    // push it again
    val err = status(postUnderpayments(n1, p1, Underpayments(List(u1))))

    // check they 409 out (DUPLICATE)
    err shouldBe CONFLICT
  }

  test("\"POST\" Underpayments should store Underpayments") {
    val n1 = "AS000001A"
    val p1 = "1519948e-8a54-11ec-8ed1-5bb13a0b0e94"

    status(postUnderpayments(n1, p1, Underpayments(List(u1, u2, u3)))) shouldBe OK

    val response = countUnderpayments(n1, UUID.fromString(p1))
    contentAsString(response) shouldBe """{"count":3}"""
  }

  test("\"DELETE\" Underpayments should delete all Underpayments") {
    val n1 = "AS000002A"
    val p1 = "1519948e-8a54-11ec-8ed1-5bb13a0b0e94"
    status(postUnderpayments(n1, p1, Underpayments(List(u1, u2, u3)))) shouldBe OK
    val n2 = "AS000003A"
    val p2 = "1519948e-8a54-11ec-8ed1-5bb13a0b0e95"
    status(postUnderpayments(n2, p2, Underpayments(List(u1, u2, u3)))) shouldBe OK

    status(deleteAllUnderpayments()) shouldBe OK

    val response = countUnderpayments(n1, UUID.fromString(p1))
    contentAsString(response) shouldBe """{"count":0}"""
    val response2 = countUnderpayments(n2, UUID.fromString(p2))
    contentAsString(response2) shouldBe """{"count":0}"""
  }

  test("\"GET\" Underpayments should get a bunch of Underpayments") {
    val n1 = "AS000002A"
    val p1 = "1519948e-8a54-11ec-8ed1-5bb13a0b0e94"
    status(postUnderpayments(n1, p1, Underpayments(List(u1, u2, u3)))) shouldBe OK

    val response = getUnderpayments(n1, UUID.fromString(p1))

    status(response) shouldBe OK
    val underpayments = contentAsJson(response).as[Underpayments].underPayments
    underpayments should contain theSameElementsAs List(u1, u2, u3)
  }
}
