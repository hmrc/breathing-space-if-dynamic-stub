import play.core.PlayVersion.current
import sbt.*

object Dependencies {

  val playVersion = "play-28"
  val bootstrapVersion = "7.22.0"
  val hmrcMongoVersion = "1.3.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-backend-$playVersion"    % bootstrapVersion,
    "org.typelevel"                %% "cats-core"                  % "2.10.0",
    "com.beachape"                 %% "enumeratum"                 % "1.7.3",
    "ai.x"                         %% "play-json-extensions"       % "0.42.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.15.3",
    "com.github.andyglow"          %% "scala-jsonschema"           % "0.7.11",
    "uk.gov.hmrc.mongo"            %% s"hmrc-mongo-$playVersion"   % hmrcMongoVersion
)

  val test = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "com.vladsch.flexmark"   %  "flexmark-all"           % "0.64.8",
    "com.typesafe.play"      %% "play-test"              % current,
    "org.scalatest"          %% "scalatest"              % "3.2.17",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0"
  ).map(_ % "test,it")
}
