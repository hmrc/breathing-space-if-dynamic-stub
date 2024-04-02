import sbt._


object Dependencies {

  val playVersion: String          = "play-30"
  val bootstrapVersion: String     = "8.5.0"
  val hmrcMongoVersion: String     = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-backend-$playVersion"   % bootstrapVersion,
    "org.typelevel"                %% "cats-core"                         % "2.10.0",
    "com.github.arturopala"        %% "play-json-extensions"              % "1.0.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"              % "2.17.0",
    "com.github.andyglow"          %% "scala-jsonschema"                  % "0.7.11",
    "uk.gov.hmrc.mongo"            %% s"hmrc-mongo-$playVersion"          % hmrcMongoVersion
)

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "com.typesafe.play"      %% "play-test"                     % "2.9.1",
    "org.scalatest"          %% "scalatest"                     % "3.2.18",
    "org.scalatestplus.play" %% "scalatestplus-play"            % "7.0.1"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
}
