import play.core.PlayVersion.current
import sbt._

object Dependencies {

  val compile = Seq(
    "uk.gov.hmrc"   %% "bootstrap-backend-play-27" % "2.25.0",
    "org.typelevel" %% "cats-core"                 % "2.2.0",
    "com.beachape"  %% "enumeratum"                % "1.6.1",
    "uk.gov.hmrc"   %% "simple-reactivemongo"      % "7.30.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "2.25.0" % Test,
    "org.scalatest"           %% "scalatest"                % "3.1.2"                 % Test,
    "com.typesafe.play"       %% "play-test"                % current                 % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.35.10"               % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"                 % "test, it"
  )
}
