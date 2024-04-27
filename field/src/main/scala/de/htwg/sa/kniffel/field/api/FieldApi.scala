package de.htwg.sa.kniffel.field.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import de.htwg.sa.kniffel.field.model.IField
import play.api.libs.json.{JsBoolean, JsNull, JsNumber, Json}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class FieldApi(using field: IField):

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  Http().newServerAt("localhost", 9001).bind(
    pathPrefix("field") {
      val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
        val ints = str.split("=").tail.mkString(",").split(",").map(_.toInt)
        Some(ints.toList)
      }
      concat(
        get {
          concat(
            pathSingleSlash {
              complete(field.newField(2).toJson.toString)
            },
            path("ping") {
              complete("pong")
            },
            path("new" / IntNumber) {
              (numberOfPlayers: Int) =>
                complete(field.newField(numberOfPlayers).toJson.toString)
            },
            path("") {
              sys.error("No such GET route")
            }
          )
        },
        post {
          concat(
            // example: putMulti/list=1,0,1,0,1,1/1/0/0 (also with field as Json String in request body)
            path("putMulti" / IntList / IntNumber / IntNumber / IntNumber) {
              (valueList: List[Int], putInValue: Int, x: Int, y: Int) =>
                entity(as[String]) { requestBody =>
                  complete(field.jsonStringToField(requestBody).putMulti(valueList, Some(putInValue), x, y).toJson.toString)
                }
            },
            // example: undoMove/list=1,0,1,0,1,1/0/0 (also with field as Json String in request body)
            path("undoMove" / IntList / IntNumber / IntNumber) {
              (valueList: List[Int], x: Int, y: Int) =>
                entity(as[String]) { requestBody =>
                  complete(field.jsonStringToField(requestBody).undoMove(valueList, x, y).toJson.toString)
                }
            },
            path("cell" / IntNumber / IntNumber) { (col: Int, row: Int) =>
              entity(as[String]) { requestBody =>
                complete(
                  field.jsonStringToField(requestBody).matrix.cell(col, row).match {
                    case Some(value) => Json.obj("value" -> JsNumber(value)).toString
                    case None => Json.obj("value" -> JsNull).toString
                  }
                )
              }
            },
            path("numberOfPlayers") {
              entity(as[String]) { requestBody =>
                complete(Json.obj("numberOfPlayers" -> JsNumber(field.jsonStringToField(requestBody).numberOfPlayers)).toString)
              }
            },
            path("isEmpty" / IntNumber / IntNumber) {
              (col: Int, row: Int) =>
                entity(as[String]) { requestBody =>
                  complete(
                    Json.obj(
                      "isEmpty" -> JsBoolean(field.jsonStringToField(requestBody).matrix.isEmpty(col, row))
                    ).toString
                  )
                }
            },
            path("mesh") {
              entity(as[String]) { requestBody =>
                complete(field.jsonStringToField(requestBody).toString)
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