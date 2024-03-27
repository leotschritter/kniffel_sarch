import org.scoverage.coveralls.GitHubActions

val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "kniffel",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.10",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
    libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.0-RC5")
  )

import org.scoverage.coveralls.Imports.CoverallsKeys._

coverallsTokenFile := sys.env.get("COVERALLS_REPO_TOKEN")
coverallsService := Some(GitHubActions)

coverageHighlighting := true
coverageFailOnMinimum := false
coverageMinimumStmtTotal := 0
coverageMinimumBranchTotal := 0
coverageMinimumStmtPerPackage := 0
coverageMinimumBranchPerPackage := 0
coverageMinimumStmtPerFile := 0
coverageMinimumBranchPerFile := 0