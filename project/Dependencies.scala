import sbt.*


object Dependencies {

  val playVersion: String          = "play-30"
  val bootstrapVersion: String     = "10.4.0"
  val hmrcMongoVersion: String     = "2.10.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-backend-$playVersion"   % bootstrapVersion,
    "org.typelevel"                %% "cats-core"                         % "2.13.0",
    "uk.gov.hmrc.mongo"            %% s"hmrc-mongo-$playVersion"          % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.playframework"      %% "play-test"                     % "3.0.9",
    "org.scalatest"          %% "scalatest"                     % "3.2.19",
    "org.scalatestplus.play" %% "scalatestplus-play"            % "7.0.2"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
}
