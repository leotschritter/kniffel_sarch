package de.htwg.sa.kniffel.gui.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import de.htwg.sa.kniffel.gui.aview.GUI

import scala.concurrent.ExecutionContext

class GuiApi(gui: GUI):
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  Http().newServerAt("localhost", 9004).bind(
    pathPrefix("gui") {
      concat(
        put {
          concat(
            path("quit") {
              complete(gui.update("quit"))
            },
            path("save") {
              complete(gui.update("save"))
            },
            path("load") {
              complete(gui.update("load"))
            },
            path("move") {
              complete(gui.update("move"))
            },
          )
        }
      )
    }
  )
