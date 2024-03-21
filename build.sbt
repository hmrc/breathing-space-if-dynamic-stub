import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

val appName = "breathing-space-if-dynamic-stub"

val silencerVersion = "1.7.1"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / scalafmtOnCompile := true

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(
    scoverageSettings,
    scalaSettings,
    defaultSettings(),
    TwirlKeys.templateImports := Seq(),
    libraryDependencies ++= Dependencies.all,
    PlayKeys.playDefaultPort := 9503,
    scalacOptions ++= Seq(
      "-Werror",
      "-Wconf:cat=unused-imports&site=.*views\\.html.*:s",
      "-Wconf:cat=unused-imports&site=<empty>:s",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s",
      "-deprecation",
      "-feature",
      "-unchecked"
    ),
    assemblySettings
  )
  .settings(resolvers += Resolver.jcenterRepo)

Compile / unmanagedResourceDirectories += baseDirectory.value / "public"

lazy val scoverageSettings = {

  val ScoverageExclusionPatterns = List(
    "<empty>",
    "uk\\.gov\\.hmrc\\.breathingspaceifproxy\\.views\\..*",
    ".*(Reverse|AuthService|BuildInfo|Routes).*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := ScoverageExclusionPatterns
      .mkString("", ";", ""),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

Test / Keys.fork := true
Test / parallelExecution := true

lazy val it = project
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(
    libraryDependencies ++= Dependencies.testAndIt,
    DefaultBuildSettings.itSettings()
  )

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := "breathing-space-if-dynamic-stub.jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
)
