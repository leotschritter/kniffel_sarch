package it.dicecup

import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import io.gatling.jdbc.Predef.*

import scala.concurrent.duration.*

class DiceCupLoadItSimulation extends Simulation {

  private val userAmount = 10
  private val fullDuration = 20.seconds
  private val httpProtocol = http
    .baseUrl("http://localhost:9002")
    .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.37.3")

  private val headers_0 = Map(
    "Cache-Control" -> "no-cache",
    "Postman-Token" -> "9deabaf5-ef79-4a25-9bc2-c45f5e023172"
  )

  private val headers_1 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "b08eb3de-9c38-4c1f-b6bd-8651aa6deef0"
  )

  private val headers_2 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "6d12c91c-83c1-4b9f-b936-180247f51a48"
  )

  private val headers_3 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "d7adb47e-fb58-4f8e-9bec-c73ad9ac77a1"
  )

  private val headers_4 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "c0fa7a81-518e-4bed-8816-4ae1799f03ff"
  )

  private val headers_5 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "533cf05e-5b1a-4314-b802-ac38561915c1"
  )

  private val headers_6 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "eb252bf2-8540-4ffc-86f7-3b9c4f484111"
  )

  private val headers_7 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "b01e5c31-b36c-4f78-be5f-d810ac380e49"
  )

  private val headers_8 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "b469ee2d-0036-4182-8710-7400d1944c9f"
  )

  private val headers_9 = Map(
    "Cache-Control" -> "no-cache",
    "Content-Type" -> "application/json",
    "Postman-Token" -> "e9649ca6-a179-4d3b-bcfa-f361c911ac5c"
  )


  private val scn = scenario("RecordedSimulation")
    .exec(
      http("indexOfField")
        .get("/diceCup/indexOfField")
        .headers(headers_0),
      pause(2),
      http("dice")
        .post("/diceCup/dice")
        .headers(headers_1)
        .body(RawFileBody("dicecup/0001_request.txt")),
      pause(2),
      http("representation")
        .post("/diceCup/representation")
        .headers(headers_2)
        .body(RawFileBody("dicecup/0002_request.txt")),
      pause(2),
      http("putOut")
        .post("/diceCup/putOut/list=1,4,3,1,6")
        .headers(headers_3)
        .body(RawFileBody("dicecup/0003_request.txt")),
      pause(3),
      http("putIn")
        .post("/diceCup/putIn/list=1,4,3,1,6")
        .headers(headers_4)
        .body(RawFileBody("dicecup/0004_request.txt")),
      pause(2),
      http("nextRound")
        .post("/diceCup/nextRound")
        .headers(headers_5)
        .body(RawFileBody("dicecup/0005_request.txt")),
      pause(2),
      http("inCup")
        .post("/diceCup/inCup")
        .headers(headers_6)
        .body(RawFileBody("dicecup/0006_request.txt")),
      pause(2),
      http("locked")
        .post("/diceCup/locked")
        .headers(headers_7)
        .body(RawFileBody("dicecup/0007_request.txt")),
      pause(2),
      http("remainingDices")
        .post("/diceCup/remainingDices")
        .headers(headers_8)
        .body(RawFileBody("dicecup/0008_request.txt")),
      pause(2),
      http("result")
        .post("/diceCup/result/0")
        .headers(headers_9)
        .body(RawFileBody("dicecup/0009_request.txt"))
    )

  setUp(scn.inject(rampUsers(userAmount).during(fullDuration))).protocols(httpProtocol)
}
