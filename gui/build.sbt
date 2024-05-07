name := "gui"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val dependencies = Seq(
  libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.18",
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.5"),
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.3",
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.5",
  libraryDependencies += "org.apache.cassandra" % "cassandra-all" % "4.1.4" excludeAll(
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
    ExclusionRule(organization = "log4j", name = "log4j")
  ),

)


lazy val gui = (project in file("."))
  .settings(
    dependencies
  )
