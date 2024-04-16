import org.scoverage.coveralls.GitHubActions

val scala3Version = "3.2.0"

val dicecupVersion = "0.1.0-SNAPSHOT"
val fieldVersion = "0.1.0-SNAPSHOT"
val gameVersion = "0.1.0-SNAPSHOT"
val fileIOVersion = "0.1.0-SNAPSHOT"

lazy val dependencies = Seq(
  scalaVersion := scala3Version,
  libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.10",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test",
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.0-RC5")
)

lazy val root = (project in file(""))
  .dependsOn(dicecup, fileIO, field, game)
  .aggregate(dicecup, fileIO, field, game)
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

