import sbt.{Resolver, *}
import Keys.*
import play.sbt.PlayImport.*
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

val appName = "breathing-space-if-dynamic-stub"

val silencerVersion = "1.7.1"

lazy val scoverageSettings = {
  val ScoverageExclusionPatterns = List(
    "<empty>",
    "uk\\.gov\\.hmrc\\.breathingspaceifproxy\\.views\\..*",
    ".*(Reverse|AuthService|BuildInfo|Routes).*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := ScoverageExclusionPatterns.mkString("", ";", ""),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

ThisBuild / majorVersion := 2
ThisBuild / scalaVersion := "3.3.4"
ThisBuild / scalafmtOnCompile := true

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(
    name := appName,
    PlayKeys.playDefaultPort := 9503,
    scoverageSettings,
    scalaSettings,
    libraryDependencies ++= Dependencies.all,
    defaultSettings(),
    TwirlKeys.templateImports := Seq(),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Werror",
      "-Wvalue-discard",
      "-language:noAutoTupling",
      "-Wconf:msg=unused import&src=.*views\\.html.*:s",
      "-Wconf:msg=unused import&src=<empty>:s",
      "-Wconf:msg=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:msg=unused&src=.*Routes\\.scala:s",
      "-Wconf:msg=unused&src=.*ReverseRoutes\\.scala:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    ),
    scalacOptions := scalacOptions.value.filterNot(_ contains "-Wunused"),
      resolvers += Resolver.jcenterRepo
  )

Test / fork := true
Test / parallelExecution := true

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    libraryDependencies ++= Dependencies.test,
    DefaultBuildSettings.itSettings()
  )

Compile / unmanagedResourceDirectories += baseDirectory.value / "public"
