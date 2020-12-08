import play.core.PlayVersion.current
import sbt._

object Dependencies {

  object version {
    val jackson = "2.11.3"
  }

  val compile = Seq(
    "com.fasterxml.jackson.core"     %  "jackson-annotations"      % version.jackson,
    "com.fasterxml.jackson.core"     %  "jackson-core"             % version.jackson,
    "com.fasterxml.jackson.core"     %  "jackson-databind"         % version.jackson,
    "com.fasterxml.jackson.datatype" %  "jackson-datatype-jdk8"    % version.jackson,
    "com.fasterxml.jackson.datatype" %  "jackson-datatype-jsr310"  % version.jackson,
    "com.fasterxml.jackson.module"   %  "jackson-module-parameter-names" % version.jackson,
    "com.fasterxml.jackson.module"   %  "jackson-module-paranamer" % version.jackson,
    "com.fasterxml.jackson.module"   %% "jackson-module-scala"     % version.jackson,

    "uk.gov.hmrc"         %% "bootstrap-backend-play-27"  % "3.0.0",
    "org.typelevel"       %% "cats-core"                  % "2.2.0",
    "com.beachape"        %% "enumeratum"                 % "1.6.1",
    "ai.x"                %% "play-json-extensions"       % "0.42.0",
    "com.github.andyglow" %% "scala-jsonschema"           % "0.4.0",
    "uk.gov.hmrc"         %% "raml-tools"                 % "1.18.0",
    "uk.gov.hmrc"         %% "simple-reactivemongo"       % "7.30.0-play-27"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-27" % "3.0.0" % "it",
    "com.vladsch.flexmark"   %  "flexmark-all"           % "0.36.8" % "it",
    "com.typesafe.play"      %% "play-test"              % current  % "it",
    "org.scalatest"          %% "scalatest"              % "3.2.3"  % "it",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "4.0.3"  % "it"
  )
}
