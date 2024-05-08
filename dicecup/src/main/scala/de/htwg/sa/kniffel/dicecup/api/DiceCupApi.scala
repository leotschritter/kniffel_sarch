package de.htwg.sa.kniffel.dicecup.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1}
import de.htwg.sa.kniffel.dicecup.model.IDiceCup
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsNull, JsNumber, Json}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class DiceCupApi(using diceCup: IDiceCup):
  private val log: Logger = LoggerFactory.getLogger(classOf[DiceCupApi])

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
    val ints = str.split("=").tail.mkString(",").split(",").map(_.toInt)
    Some(ints.toList)
  }

  Http().newServerAt("localhost", 9002).bind(
    pathPrefix("diceCup") {
      concat(
        get {
          concat(
            pathSingleSlash {
              complete(diceCup.toJson.toString)
            },
            path("ping") {
              complete("pong")
            },
            path("indexOfField") {
              complete(Json.obj("indexOfField" -> diceCup.indexOfField).toString)
            },
            path("") {
              sys.error("No such GET route")
            }
          )
        },
        post {
          concat(
            path("inCup") {
              entity(as[String]) { requestBody =>
                complete(Json.obj("inCup" -> diceCup.jsonStringToDiceCup(requestBody).inCup).toString)
              }
            },
            path("locked") {
              entity(as[String]) { requestBody =>
                complete(Json.obj("locked" -> diceCup.jsonStringToDiceCup(requestBody).locked).toString)
              }
            },
            path("remainingDices") {
              entity(as[String]) { requestBody =>
                complete(Json.obj("remainingDices" -> JsNumber(diceCup.jsonStringToDiceCup(requestBody).remainingDices)).toString)
              }
            },
            path("result" / IntNumber) { (index: Int) =>
              entity(as[String]) { requestBody =>
                complete(Json.obj("result" -> JsNumber(diceCup.jsonStringToDiceCup(requestBody).result(index))).toString)
              }
            },
            path("nextRound") {
              entity(as[String]) { requestBody =>
                complete(diceCup.jsonStringToDiceCup(requestBody).nextRound().toJson.toString)
              }
            },
            // example: putOut/list=1,2,3
            path("putOut" / IntList) { (list: List[Int]) =>
              entity(as[String]) { requestBody =>
                complete(diceCup.jsonStringToDiceCup(requestBody).putDicesOut(list).toJson.toString)
              }
            },
            path("putIn" / IntList) { (list: List[Int]) =>
              entity(as[String]) { requestBody =>
                complete(diceCup.jsonStringToDiceCup(requestBody).putDicesIn(list).toJson.toString)
              }
            },
            path("dice") {
              entity(as[String]) { requestBody =>
                complete(
                  diceCup.jsonStringToDiceCup(requestBody).dice().match {
                    case Some(diceCup) => diceCup.toJson.toString
                    case None => Json.obj("dicecup" -> JsNull).toString
                  }
                )
              }
            },
            path("representation") {
              entity(as[String]) { requestBody =>
                complete(diceCup.jsonStringToDiceCup(requestBody).toString)
              }
            },
            path("") {

              sys.error("No such POST route")
            }
          )
        }
      )
    }
  )

  def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)
