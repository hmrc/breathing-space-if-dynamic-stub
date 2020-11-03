package uk.gov.hmrc.breathingspaceifstub.controller

import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class PeriodsControllerISpec extends BaseISpec {

  test("\"postPeriods\" should successfully add a single period for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriodsInRequest = List(genPostPeriodInRequest(true))
    status(postPeriods(individual.nino, postPeriodsInRequest)) shouldBe CREATED
  }

  test("\"postPeriods\" should successfully add multiple periods for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriodsInRequest = List(genPostPeriodInRequest(true), genPostPeriodInRequest(false))
    status(postPeriods(individual.nino, postPeriodsInRequest)) shouldBe CREATED
  }

  test("\"postPeriods\" should report if the provided Nino is unknown") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriodsInRequest = List(genPostPeriodInRequest(true))
    status(postPeriods(genNino, postPeriodsInRequest)) shouldBe NOT_FOUND
  }
}
