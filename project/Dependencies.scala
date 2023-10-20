import play.core.PlayVersion.current
import sbt._

object Dependencies {

  val bootstrapVersion = "7.22.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "org.typelevel"                %% "cats-core"                  % "2.10.0",
    "com.beachape"                 %% "enumeratum"                 % "1.7.3",
    "ai.x"                         %% "play-json-extensions"       % "0.42.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.15.3",
    "com.github.andyglow"          %% "scala-jsonschema"           % "0.7.11",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % "1.3.0"
)

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapVersion  % "it",
    "com.vladsch.flexmark"   %  "flexmark-all"           % "0.64.8" % "it",
    "com.typesafe.play"      %% "play-test"              % current  % "it",
    "org.scalatest"          %% "scalatest"              % "3.2.17" % "it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0"  % "it"
  )
}
