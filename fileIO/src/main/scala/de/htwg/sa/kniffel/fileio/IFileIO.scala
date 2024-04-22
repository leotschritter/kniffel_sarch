package de.htwg.sa.kniffel.fileio

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

trait IFileIO {
  def loadField: String

  def loadGame: String

  def loadDiceCup: String

  def saveField(field: String): String

  def saveGame(game: String): String

  def saveDiceCup(diceCup: String): String

  val fileIORoute: Route =
    concat(
      get {
        concat(
          path("loadField") {
            complete(loadField)
          },
          path("loadGame") {
            complete(loadGame)
          },
          path("loadDiceCup") {
            complete(loadDiceCup)
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
              complete(saveField(requestBody))
            }
          },
          path("saveGame") {
            entity(as[String]) { requestBody =>
              complete(saveGame(requestBody))
            }
          },
          path("saveDiceCup") {
            entity(as[String]) { requestBody =>
              complete(saveDiceCup(requestBody))
            }
          },
          path("") {
            sys.error("No such POST route")
          }
        )
      }
    )
}
