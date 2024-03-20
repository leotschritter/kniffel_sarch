name := """Kniffel"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-RC2" % Test
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.1.1"
// WebJars Dependency for jQuery
libraryDependencies += "org.webjars" % "jquery" % "3.6.0"
PlayKeys.devSettings += "play.server.websocket.periodic-keep-alive-max-idle" -> "10 seconds"
PlayKeys.devSettings += "play.server.websocket.periodic-keep-alive-mode" -> "ping"
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "scala.de.htwg.wapp.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "scala.de.htwg.wapp.binders._"
