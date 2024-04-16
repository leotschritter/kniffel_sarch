import org.scoverage.coveralls.GitHubActions

val scala3Version = "3.3.1"

val dicecupVersion = "0.1.0-SNAPSHOT"
val fieldVersion = "0.1.0-SNAPSHOT"
val gameVersion = "0.1.0-SNAPSHOT"
val fileIOVersion = "0.1.0-SNAPSHOT"
val controllerVersion = "0.1.0-SNAPSHOT"
val utilVersion = "0.1.0-SNAPSHOT"

lazy val dependencies = Seq(
  scalaVersion := scala3Version,
  libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.18",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test",
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.4"),
  libraryDependencies += "com.google.inject" % "guice" % "7.0.0",
  libraryDependencies += "net.codingwell" %% "scala-guice" % "7.0.0"
)

lazy val root = (project in file(""))
  .dependsOn(dicecup, fileIO, field, game, controller, util)
  .aggregate(dicecup, fileIO, field, game, controller, util)
  .settings(
    name := "kniffel",
    version := "0.1.0-SNAPSHOT",
    dependencies
  )

lazy val dicecup = (project in file("dicecup"))
  .settings(
    name := "dicecup",
    version := dicecupVersion,
    dependencies
  )

lazy val field = (project in file("field"))
  .settings(
    name := "field",
    version := fieldVersion,
    dependencies
  )

lazy val game = (project in file("game"))
  .settings(
    name := "game",
    version := gameVersion,
    dependencies
  )

lazy val fileIO = (project in file("fileIO"))
  .dependsOn(dicecup, field, game)
  .aggregate(dicecup, field, game)
  .settings(
    name := "fileIO",
    version := fileIOVersion,
    dependencies
  )

lazy val controller = (project in file("controller"))
  .dependsOn(game, field, dicecup, util, fileIO)
  .aggregate(game, field, dicecup, util, fileIO)
  .settings(
    name := "controller",
    version := controllerVersion,
    dependencies
  )

lazy val util = (project in file("util"))
  .dependsOn(game, field)
  .aggregate(game, field)
  .settings(
    name := "util",
    version := utilVersion,
    dependencies
  )

import org.scoverage.coveralls.Imports.CoverallsKeys.*

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

