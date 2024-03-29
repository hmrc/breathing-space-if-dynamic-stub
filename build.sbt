import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName = "breathing-space-if-dynamic-stub"

val silencerVersion = "1.7.1"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(
    majorVersion             := 0,
    scalaVersion             := "2.13.8",
    PlayKeys.playDefaultPort := 9503,
    TwirlKeys.templateImports := Seq(),
    libraryDependencies      ++= Dependencies.compile ++ Dependencies.test,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked"
    ),
    assemblySettings
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    scoverageSettings,
    Compile / scalafmtOnCompile := true
  )

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

Compile / unmanagedResourceDirectories += baseDirectory.value / "public"

IntegrationTest / unmanagedResourceDirectories += baseDirectory.value / "it" / "resources"

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>",
    "uk\\.gov\\.hmrc\\.breathingspaceifproxy\\.views\\..*",
    ".*(Reverse|AuthService|BuildInfo|Routes).*"
  ).mkString(";"),
  coverageMinimumStmtTotal := 90,
  coverageFailOnMinimum := false,
  coverageHighlighting := true
)

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := "breathing-space-if-dynamic-stub.jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
)
