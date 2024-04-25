package de.htwg.sa.kniffel.fileio.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import de.htwg.sa.kniffel.fileio.model.IFileIO

import scala.concurrent.ExecutionContext

class FileIoApi @Inject()(fileIO: IFileIO):
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher


  Http().newServerAt("localhost", 9000).bind(
    pathPrefix("io") {
      concat(
        get {
          concat(
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
