package it.persistence

import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

class PersistenceVolumeItSimulation extends Simulation {
  private val userAmount = 500
  private val httpProtocol = http
    .baseUrl("http://localhost:9000")
    .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.37.3")

  private val headers_0 = Map("Postman-Token" -> "5bb8b916-7444-4522-a1fd-b8c915b17600")

  private val headers_1 = Map(
    "Content-Type" -> "application/json",
    "Postman-Token" -> "98e6add4-cf75-46ec-a2dd-39c07606cd84"
  )

  private val headers_2 = Map(
    "Content-Type" -> "application/json",
    "Postman-Token" -> "e84d19ff-de7c-4259-83ab-aa4b48646622"
  )

  private val headers_3 = Map(
    "Content-Type" -> "application/json",
    "Postman-Token" -> "438b350f-dec7-4b8e-897d-f45508e83dc8"
  )

  private val headers_4 = Map("Postman-Token" -> "ad089f7c-502d-4d56-baf7-64c8e221bafa")

  private val headers_5 = Map("Postman-Token" -> "dd170687-8043-4064-8f4b-2a85e4ee38b2")

  private val headers_6 = Map("Postman-Token" -> "e1a1fc55-5d66-4e7b-9c01-b1304a81013f")


  private val scn = scenario("RecordedSimulation")
    .exec(
      http("ping")
        .get("/io/ping")
        .headers(headers_0),
      pause(8),
      http("saveField")
        .post("/io/saveField")
        .headers(headers_1)
        .body(RawFileBody("persistence/0001_request.txt")),
      pause(3),
      http("saveGame")
        .post("/io/saveGame")
        .headers(headers_2)
        .body(RawFileBody("persistence/0002_request.txt")),
      pause(4),
      http("saveDiceCup")
        .post("/io/saveDiceCup")
        .headers(headers_3)
        .body(RawFileBody("persistence/0003_request.txt")),
      pause(9),
      http("loadGame")
        .get("/io/loadGame/2")
        .headers(headers_4),
      pause(9),
      http("loadDiceCup")
        .get("/io/loadDiceCup/2")
        .headers(headers_5),
      pause(21),
      http("loadField")
        .get("/io/loadField/2")
        .headers(headers_6)
    )

  setUp(scn.inject(atOnceUsers(userAmount))).protocols(httpProtocol)
}
