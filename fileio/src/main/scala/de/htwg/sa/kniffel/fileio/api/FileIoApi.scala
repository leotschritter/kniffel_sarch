package de.htwg.sa.kniffel.fileio.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import de.htwg.sa.kniffel.fileio.model.IFileIO

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class FileIoApi(using fileIO: IFileIO):
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
              complete(fileIO.loadField)
            },
            path("loadGame") {
              complete(fileIO.loadGame)
            },
            path("loadDiceCup") {
              complete(fileIO.loadDiceCup)
            },
            path("") {
              sys.error("No such GET route")
            }
          )
        },
        post {
          concat(
            path("saveField") {
              entity(as[String]) { requestBody =>
                complete(fileIO.saveField(requestBody))
              }
            },
            path("saveGame") {
              entity(as[String]) { requestBody =>
                complete(fileIO.saveGame(requestBody))
              }
            },
            path("saveDiceCup") {
              entity(as[String]) { requestBody =>
                complete(fileIO.saveDiceCup(requestBody))
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