import play.core.PlayVersion.current
import sbt._

object Dependencies {

  val compile = Seq(
    "uk.gov.hmrc"         %% "bootstrap-backend-play-27"  % "2.25.0",
    "org.typelevel"       %% "cats-core"                  % "2.2.0",
    "com.beachape"        %% "enumeratum"                 % "1.6.1",
    "ai.x"                %% "play-json-extensions"       % "0.42.0",
    "com.github.andyglow" %% "scala-jsonschema"           % "0.4.0",
    "uk.gov.hmrc"         %% "raml-tools"                 % "1.18.0",
    "uk.gov.hmrc"         %% "simple-reactivemongo"       % "7.30.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-27" % "2.25.0" % "it",
    "com.vladsch.flexmark"   %  "flexmark-all"           % "0.36.8" % "it",
    "com.typesafe.play"      %% "play-test"              % current  % "it",
    "org.scalatest"          %% "scalatest"              % "3.2.0"  % "it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "4.0.3"  % "it"
  )
}
