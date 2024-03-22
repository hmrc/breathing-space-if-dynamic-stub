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

package controller

import play.api.http.Status.{BAD_GATEWAY, CONFLICT, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.test.Helpers.{contentAsJson, contentAsString, status}
import support.BaseISpec
import uk.gov.hmrc.breathingspaceifstub.model.Underpayments

import java.util.UUID

class UnderpaymentsControllerISpec extends BaseISpec {

  override def beforeEach: Unit =
    status(deleteAllUnderpayments()) shouldBe OK

  test("\"POST\" Underpayments should not allow duplicates") {

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

  test("\"GET\" Underpayments should get 204 (NO_CONTENT) response for an empty underpayment list ") {
    val n1 = "AS000001B"
    val p1 = "a55d2098-61b3-11ec-9ff0-60f262c313dd"
    status(postUnderpayments(n1, p1, Underpayments(List()))) shouldBe OK

    val response = getUnderpayments(n1, UUID.fromString(p1))

    status(response) shouldBe NO_CONTENT
  }

  test("\"get\" Underpayments should return an Error INTERNAL_SERVER_ERROR(500) for the nino BS000500C") {
    val nino = "BS000500C"
    val response = getMemorandum(nino)
    status(response) shouldBe INTERNAL_SERVER_ERROR
  }

  test("\"get\" Underpayments should return an Error BAD_GATEWAY(502) for the nino BS000502C") {
    val nino = "BS000502C"
    val response = getMemorandum(nino)
    status(response) shouldBe BAD_GATEWAY
  }

  test("\"get\" Underpayments should return an Error SERVICE_UNAVAILABLE(503) for the nino BS000504C") {
    val nino = "BS000504C"
    val response = getMemorandum(nino)
    status(response) shouldBe GATEWAY_TIMEOUT
  }
}
