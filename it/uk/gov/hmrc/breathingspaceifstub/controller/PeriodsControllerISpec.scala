package uk.gov.hmrc.breathingspaceifstub.controller

import play.api.test.Helpers._
import uk.gov.hmrc.breathingspaceifstub.model.Periods
import uk.gov.hmrc.breathingspaceifstub.support.BaseISpec

class PeriodsControllerISpec extends BaseISpec {

  val noEndDate = false
  val withEndDate = true

  test("\"get\" Periods should return all periods for an existing Nino, if any") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriod1 = genPostPeriodInRequest(withEndDate)
    val postPeriod2 = genPostPeriodInRequest(noEndDate)
    status(postPeriods(individual.nino, List(postPeriod1, postPeriod2))) shouldBe CREATED

    val response = getPeriods(individual.nino)
    status(response) shouldBe OK

    val periods = contentAsJson(response).as[Periods].periods
    periods.size shouldBe 2

    periods.head.startDate shouldBe postPeriod1.startDate
    periods.head.endDate shouldBe postPeriod1.endDate

    periods.last.startDate shouldBe postPeriod2.startDate
    periods.last.endDate shouldBe postPeriod2.endDate
  }

  test("\"get\" Periods should return an empty list if no one is found for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val response = getPeriods(individual.nino)
    status(response) shouldBe OK
    contentAsString(response) shouldBe """{"periods":[]}"""
  }

  test("\"get\" Periods should report if the provided Nino is unknown") {
    status(getPeriods(genNino)) shouldBe NOT_FOUND
  }

  test("\"post\" Periods should successfully add a single period for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriodsInRequest = List(genPostPeriodInRequest(withEndDate))
    status(postPeriods(individual.nino, postPeriodsInRequest)) shouldBe CREATED
  }

  test("\"post\" Periods should successfully add multiple periods for the provided Nino") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriodsInRequest = List(
      genPostPeriodInRequest(withEndDate),
      genPostPeriodInRequest(noEndDate)
    )
    status(postPeriods(individual.nino, postPeriodsInRequest)) shouldBe CREATED
  }

  test("Multiple \"post\" Periods ops to the same Nino should append all provided periods") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriodsInRequest1 = List(genPostPeriodInRequest(noEndDate))
    status(postPeriods(individual.nino, postPeriodsInRequest1)) shouldBe CREATED

    val postPeriodsInRequest2 = List(genPostPeriodInRequest(withEndDate), genPostPeriodInRequest(noEndDate))
    status(postPeriods(individual.nino, postPeriodsInRequest2)) shouldBe CREATED

    val response = getPeriods(individual.nino)
    status(response) shouldBe OK

    val periods = contentAsJson(response).as[Periods].periods
    periods.size shouldBe 3
  }

  test("\"post\" Periods should report if the provided Nino is unknown") {
    val individual = genIndividualInRequest()
    status(postIndividual(individual)) shouldBe OK

    val postPeriodsInRequest = List(genPostPeriodInRequest(withEndDate))
    status(postPeriods(genNino, postPeriodsInRequest)) shouldBe NOT_FOUND
  }
}
