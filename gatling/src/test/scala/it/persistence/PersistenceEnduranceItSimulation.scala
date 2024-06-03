package it.persistence

import scala.concurrent.duration.*
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

class PersistenceEnduranceItSimulation extends Simulation {

  private val userAmount = 500
  private val fullDuration = 120.seconds
  private val httpProtocol = http
    .baseUrl("http://localhost:9006")
    .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.37.3")

  private val headers_0 = Map("Postman-Token" -> "6f437230-086c-40dd-ba41-0a5b5943e10b")

  private val headers_1 = Map("Postman-Token" -> "5a25dc1a-75b4-48bd-b588-00eabed876e7")

  private val headers_2 = Map("Postman-Token" -> "fc29e023-ef09-49c5-8c96-8f1e18a8063b")

  private val headers_3 = Map("Postman-Token" -> "ed95dc97-6fc9-4ef0-b63e-ff0f581e7088")


  private val scn = scenario("RecordedSimulation")
    .exec(
      http("load Options")
        .get("/controller/loadOptions")
        .headers(headers_0),
      pause(13),
      http("save")
        .get("/controller/save")
        .headers(headers_1),
      pause(8),
      http("load")
        .get("/controller/load")
        .headers(headers_2),
      pause(58),
      http("load 2")
        .get("/controller/load/2")
        .headers(headers_3)
    )

  setUp(scn.inject(rampUsers(userAmount).during(fullDuration))).protocols(httpProtocol)
}
