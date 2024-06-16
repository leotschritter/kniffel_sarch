name := "restcontroller"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val dependencies = Seq(
  libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.18",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test",
  libraryDependencies += ("com.typesafe.play" %% "play-json" % "2.10.5"),
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.3",
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.5",
  libraryDependencies += "org.apache.cassandra" % "cassandra-all" % "4.1.4" excludeAll(
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
    ExclusionRule(organization = "log4j", name = "log4j")
  ),
  libraryDependencies += "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % Test,
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.5.1",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
    "org.postgresql" % "postgresql" % "42.7.3"
  ),
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.5.6",
    "org.slf4j" % "slf4j-api" % "2.0.12"
  ),
  libraryDependencies ++= Seq(
    ("org.apache.kafka" %% "kafka-streams-scala" % "3.7.0").cross(CrossVersion.for3Use2_13),
    "org.apache.kafka" % "kafka-clients" % "3.7.0"
  ),
  libraryDependencies ++= Seq(
    ("com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2")
  ),
)

lazy val restcontroller = (project in file("."))
  .settings(
    dependencies
  )
