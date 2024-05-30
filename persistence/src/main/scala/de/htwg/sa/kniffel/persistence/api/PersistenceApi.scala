package de.htwg.sa.kniffel.persistence.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import de.htwg.sa.kniffel.persistence.persistence.IPersistence

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PersistenceApi(using persistence: IPersistence):
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  Http().newServerAt("localhost", 9000).bind(
    pathPrefix("io") {
      concat(
        get {
          concat(
            path("ping") {
              complete("pong")
            },
            path("loadField") {
              complete(persistence.loadField)
            },
            path("loadGame") {
              complete(persistence.loadGame)
            },
            path("loadDiceCup" / IntNumber) {
              (gameId: Int) =>
              complete(persistence.loadDiceCup(gameId))
            },
            path("loadField" / IntNumber) {
              (gameId: Int) =>
              complete(persistence.loadField(gameId))
            },
            path("loadGame" / IntNumber) {
              (gameId: Int) =>
              complete(persistence.loadGame(gameId))
            },
            path("loadDiceCup") {
              complete(persistence.loadDiceCup)
            },
            path("createGame" / IntNumber) {
              (numberOfPlayers: Int) => {
                complete(persistence.createGame(numberOfPlayers))
              }
            },
            path("") {
              sys.error("No such GET route")
            },
            path("loadOptions") {
              complete(persistence.loadOptions)
            }
          )
        },
        post {
          concat(
            path("saveField") {
              entity(as[String]) { requestBody =>
                complete(persistence.saveField(requestBody))
              }
            },
            path("saveGame") {
              entity(as[String]) { requestBody =>
                complete(persistence.saveGame(requestBody))
              }
            },
            path("saveDiceCup") {
              entity(as[String]) { requestBody =>
                complete(persistence.saveDiceCup(requestBody))
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