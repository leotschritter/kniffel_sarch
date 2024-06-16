name := "tui"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

logLevel := Level.Error

lazy val dependencies = Seq(
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.4"),
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.3",
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.5",
  libraryDependencies += "org.apache.cassandra" % "cassandra-all" % "4.1.4" excludeAll(
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
    ExclusionRule(organization = "log4j", name = "log4j")
  ),
  libraryDependencies ++= Seq(
    ("org.apache.kafka" %% "kafka-streams-scala" % "3.7.0").cross(CrossVersion.for3Use2_13),
    "org.apache.kafka" % "kafka-clients" % "3.7.0"
  ),
  libraryDependencies ++= Seq(
    ("com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2")
  )
)


lazy val tui = (project in file("."))
  .settings(
    dependencies
  )
