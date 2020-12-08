package uk.gov.hmrc.breathingspaceifstub.controller

import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class DebtsControllerISpec extends BaseISpec {

  test("\"get\" (Debts) should return all debts for an existing Nino, if any") {
    val individual = genIndividualInRequest(withDebts = true)
    status(postIndividual(individual)) shouldBe CREATED

    val response = getDebts(individual.nino)
    status(response) shouldBe OK

    val debts = contentAsJson(response).as[Debts]
    debts.size shouldBe 2

    debts.head shouldBe debt1
    debts.last shouldBe debt2
  }

  test("\"get\" (Debts) should return NO_DATA_FOUND(404) if no debts are found for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe CREATED

    val response = getDebts(individual.nino)
    status(response) shouldBe NOT_FOUND
    assert(contentAsString(response).startsWith("""{"failures":[{"code":"NO_DATA_FOUND""""))
  }

  test("\"get\" Periods should report if the provided Nino is unknown") {
    val response = getDebts(genNino)
    status(response) shouldBe NOT_FOUND
    assert(contentAsString(response).startsWith("""{"failures":[{"code":"IDENTIFIER_NOT_FOUND""""))
  }
}
