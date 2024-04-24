package de.htwg.sa.kniffel.game.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import com.google.inject.Inject
import de.htwg.sa.kniffel.game.model.IGame
import play.api.libs.json.{JsNull, JsNumber, Json}

import scala.concurrent.ExecutionContext

class GameApi @Inject()(game: IGame):

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher


  Http().newServerAt("localhost", 9003).bind(
    pathPrefix("game") {
      concat(
        get {
          concat(
            pathSingleSlash {
              complete(game.newGame(2).toJson.toString)
            },
            path("new" / IntNumber) {
              (numberOfPlayers: Int) =>
                complete(game.newGame(numberOfPlayers).toJson.toString)
            },
            path("") {
              sys.error("No such GET route")
            }
          )
        },
        post {
          concat(
            path("next") {
              entity(as[String]) { requestBody =>
                complete(game.jsonStringToGame(requestBody).next()
                  .match {
                    case Some(game) => game.toJson.toString
                    case None => Json.obj("game" -> JsNull).toString
                  }
                )
              }
            },
            path("undoMove" / IntNumber / IntNumber) {
              (value: Int, y: Int) =>
                entity(as[String]) { requestBody =>
                  complete(game.jsonStringToGame(requestBody).undoMove(value, y).toJson.toString)
                }
            },
            path("sum" / IntNumber / IntNumber) {
              (value: Int, y: Int) =>
                entity(as[String]) { requestBody =>
                  complete(game.jsonStringToGame(requestBody).sum(value, y).toJson.toString)
                }
            },
            path("playerID") {
              entity(as[String]) { requestBody =>
                complete(Json.obj(
                  "playerID" -> JsNumber(game.jsonStringToGame(requestBody).playerID)
                ).toString)
              }
            },
            path("playerName") {
              entity(as[String]) { requestBody =>
                complete(Json.obj(
                  "playerName" -> game.jsonStringToGame(requestBody).playerName
                ).toString)
              }
            },
            path("playerName" / IntNumber) {
              (x: Int) =>
                entity(as[String]) { requestBody =>
                  complete(Json.obj(
                    "playerName" -> game.jsonStringToGame(requestBody).playerName(x)
                  ).toString)
                }
            },
            path("nestedList") {
              entity(as[String]) { requestBody =>
                complete(Json.obj(
                  "nestedList" -> game.jsonStringToGame(requestBody).nestedList.map(_.mkString(",")).mkString(";")
                ).toString)
              }
            },
            path("remainingMoves") {
              entity(as[String]) { requestBody =>
                complete(Json.obj(
                  "remainingMoves" -> JsNumber(game.jsonStringToGame(requestBody).remainingMoves)
                ).toString)
              }
            },
            path("players") {
              entity(as[String]) { requestBody =>
                complete(Json.obj(
                  "players" -> Json.toJson(
                    Seq(for {
                      x <- game.jsonStringToGame(requestBody).playerTuples
                    } yield {
                      Json.obj(
                        "id" -> JsNumber(x._1),
                        "name" -> x._2)
                    })
                  )
                ).toString)
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

