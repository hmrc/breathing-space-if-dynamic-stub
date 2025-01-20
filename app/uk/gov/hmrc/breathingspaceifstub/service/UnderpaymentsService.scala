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

package uk.gov.hmrc.breathingspaceifstub.service

import java.util.UUID
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

import play.api.{Logger, Logging}
import uk.gov.hmrc.breathingspaceifstub.{AsyncResponse, Response}
import uk.gov.hmrc.breathingspaceifstub.model._
import uk.gov.hmrc.breathingspaceifstub.model.BaseError.{IDENTIFIER_NOT_FOUND, INVALID_UNDERPAYMENT, RESOURCE_NOT_FOUND}
import uk.gov.hmrc.breathingspaceifstub.model.Validators.validateUnderpayment
import uk.gov.hmrc.breathingspaceifstub.repository.{UnderpaymentRecord, UnderpaymentsRepository}
import uk.gov.hmrc.breathingspaceifstub.repository.UnderpaymentRecord.parseToListOfUnderpaymentsDTOs

@Singleton
class UnderpaymentsService @Inject() (
  underpaymentsRepository: UnderpaymentsRepository
)(implicit ec: ExecutionContext)
    extends NinoValidation
    with CustomErrors
    with Logging {

  def count(nino: String, periodId: UUID): AsyncResponse[Int] = underpaymentsRepository.count(nino, periodId)

  def get(nino: String, periodId: UUID): AsyncResponse[Underpayments] = checkForCustomError(nino).getOrElse(
    stripNinoSuffixAndExecOp(nino, retrieveUnderpayments(nino, periodId))
  )

  def saveUnderpayments(
    nino: String,
    periodId: String,
    underpayments: List[Underpayment],
    logger: Logger
  ): AsyncResponse[WriteResult] = {

    logger.info(s"Service received ${underpayments.size} underpayments for ${nino}/${periodId}")

    if (underpayments.forall(u => validateUnderpayment(u))) {
      logger.info(s"Service validated ${underpayments.size} underpayments for ${nino}/${periodId}")
      underpaymentsRepository.saveUnderpayments(parseToListOfUnderpaymentsDTOs(underpayments, nino, periodId))
    } else {
      Future.successful(Left(Failure(INVALID_UNDERPAYMENT, Some("One of the underpayments was invalid"))))
    }
  }

  def removeUnderpayments(): AsyncResponse[Int] = underpaymentsRepository.removeUnderpayments()

  def removeUnderpaymentFor(nino: String): AsyncResponse[Int] =
    underpaymentsRepository.removeByNino(nino).collect { case Right(n) =>
      if (n == 0) Left(Failure(RESOURCE_NOT_FOUND)) else Right(n)
    }

  def removeUnderpaymentFor(nino: String, periodId: UUID): AsyncResponse[Int] =
    underpaymentsRepository.removeByNinoAndPeriodId(nino, periodId).collect { case Right(n) =>
      if (n == 0) Left(Failure(RESOURCE_NOT_FOUND)) else Right(n)
    }

  def createUnderpayments(ls: List[UnderpaymentRecord]): Underpayments =
    if (ls.exists(upr => upr.underpayment == None)) Underpayments(List.empty[Underpayment])
    else {
      Underpayments(
        ls.map(upr =>
          upr.underpayment match {
            case Some(up) => Underpayment(up.taxYear, up.amount, up.source)
            case _ =>
              throw new IllegalStateException("Illegal State: Empty Underpayments should have been processed already")
          }
        )
      )
    }

  private def retrieveUnderpayments(nino: String, periodId: UUID): String => AsyncResponse[Underpayments] = { _ =>
    val underpaymentDTOs: Future[Option[List[UnderpaymentRecord]]] =
      underpaymentsRepository.findUnderpayments(nino, periodId)

    val underpayments: Future[Option[Underpayments]] = underpaymentDTOs
      .map(
        _.map(ls =>
          if (ls.isEmpty) Underpayments(List.empty)
          else createUnderpayments(ls)
        )
      )
    underpayments.onComplete {
      case Success(Some(ups)) => logger.info(s"The underpayments were: $ups")
      case Success(None) => logger.info(s"No underpayments")
      case _ => logger.info("General fail")
    }
    underpayments.map(_.fold[Response[Underpayments]](Left(Failure(IDENTIFIER_NOT_FOUND)))(ups => Right(ups)))
  }
}
