enablePlugins(GatlingPlugin)

scalaVersion := "3.3.3"

scalacOptions := Seq(
  "-encoding", "UTF-8", "release:8", "-deprecation", "-feature", "-unchecked",
  "-language:implicitConversions", "-language:postfixOps"
)

val gatlingVersion = "3.11.3"
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test,it"