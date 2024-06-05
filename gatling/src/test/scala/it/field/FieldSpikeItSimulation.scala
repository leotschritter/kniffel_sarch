package it.field

import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

import scala.concurrent.duration.*

class FieldSpikeItSimulation extends Simulation {

  private val userAmount = 100
  private val fullDuration = 20.seconds
  private val httpProtocol = http
    .baseUrl("http://localhost:9001")
    .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.37.3")

  private val headers_0 = Map(
    "Cache-Control" -> "no-cache",
    "Postman-Token" -> "d644ab29-fd51-48eb-a96b-32f767535e0f"
  )

  private val headers_1 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "871ae0ce-cce4-433e-95fa-09f832b3e47d"
  )

  private val headers_2 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "b678c7d5-9749-497e-96a9-6d730d96b4d8"
  )

  private val headers_3 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "261f412b-b757-4ebd-a166-150dde2513cb"
  )

  private val headers_4 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "0f0a801c-32ae-43c9-89a4-87413f9725ea"
  )

  private val headers_5 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "9446cce0-d8c3-47e5-ae1d-fd4819d472b7"
  )

  private val headers_6 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "21ed207b-0db8-47a7-9bf3-b5019b41ba69"
  )


  private val scn = scenario("RecordedSimulation")
    .exec(
      http("new")
        .get("/field/new/4")
        .headers(headers_0),
      pause(3),
      http("putMulti")
        .post("/field/putMulti/list=1,0,1,0,1,1/1/0/0")
        .headers(headers_1)
        .body(RawFileBody("field/0001_request.txt")),
      pause(2),
      http("undoMove")
        .post("/field/undoMove/list=1,0,1,0,1,1/0/0")
        .headers(headers_2)
        .body(RawFileBody("field/0002_request.txt")),
      pause(3),
      http("cell")
        .post("/field/cell/0/0")
        .headers(headers_3)
        .body(RawFileBody("field/0003_request.txt")),
      pause(3),
      http("numberOfPlayers")
        .post("/field/numberOfPlayers")
        .headers(headers_4)
        .body(RawFileBody("field/0004_request.txt")),
      pause(3),
      http("isEmpty")
        .post("/field/isEmpty/0/0")
        .headers(headers_5)
        .body(RawFileBody("field/0005_request.txt")),
      pause(3),
      http("mesh")
        .post("/field/mesh")
        .headers(headers_6)
        .body(RawFileBody("field/0006_request.txt"))
    )

  setUp(scn.inject(
    rampUsers(2).during(10.seconds),
    atOnceUsers(userAmount),
    rampUsers(2).during(10.seconds)
  )).protocols(httpProtocol)
}
