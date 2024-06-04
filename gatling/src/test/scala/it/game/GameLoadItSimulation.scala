package it.game

import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

import scala.concurrent.duration.*

class GameLoadItSimulation extends Simulation {

  private val userAmount = 10
  private val fullDuration = 20.seconds

  private val httpProtocol = http
    .baseUrl("http://localhost:9003")
    .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.37.3")

  private val headers_0 = Map(
    "Cache-Control" -> "no-cache",
    "Postman-Token" -> "5860cacf-8e34-4de2-a6af-4d91f8041c67"
  )

  private val headers_1 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "2f15353a-bd2a-49b7-ae9c-02c0fb9e2e6e"
  )

  private val headers_2 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "493d09ce-2052-4da6-a97d-b4aefa44a6f4"
  )

  private val headers_3 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "5ee84c59-f12f-4ec7-9e4a-775aecc133d4"
  )

  private val headers_4 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "fc64289f-4715-42bf-b40b-f2c037b916cc"
  )

  private val headers_5 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "e5e4ace0-4d61-48be-8996-21539f98f21d"
  )

  private val headers_6 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "f1877223-c95e-4e72-8131-45ce2c6d150a"
  )

  private val headers_7 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "9ca165ec-ebb1-43b1-a378-d26c4af3dd66"
  )

  private val headers_8 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "60e548c5-af20-4492-973e-0a3438424fe7"
  )


  private val scn = scenario("RecordedSimulation")
    .exec(
      http("new")
        .get("/game/new/4")
        .headers(headers_0),
      pause(5),
      http("next")
        .post("/game/next")
        .headers(headers_1)
        .body(RawFileBody("game/0001_request.txt")),
      pause(3),
      http("undoMove")
        .post("/game/undoMove/40/13")
        .headers(headers_2)
        .body(RawFileBody("game/0002_request.txt")),
      pause(3),
      http("sum")
        .post("/game/sum/40/13")
        .headers(headers_3)
        .body(RawFileBody("game/0003_request.txt")),
      pause(4),
      http("playerID")
        .post("/game/playerID")
        .headers(headers_4)
        .body(RawFileBody("game/0004_request.txt")),
      pause(4),
      http("playerName")
        .post("/game/playerName")
        .headers(headers_5)
        .body(RawFileBody("game/0005_request.txt")),
      pause(4),
      http("nestedList")
        .post("/game/nestedList")
        .headers(headers_6)
        .body(RawFileBody("game/0006_request.txt")),
      pause(4),
      http("remainingMoves")
        .post("/game/remainingMoves")
        .headers(headers_7)
        .body(RawFileBody("game/0007_request.txt")),
      pause(4),
      http("players")
        .post("/game/players")
        .headers(headers_8)
        .body(RawFileBody("game/0008_request.txt"))
    )

  setUp(scn.inject(rampUsers(userAmount).during(fullDuration))).protocols(httpProtocol)
}
