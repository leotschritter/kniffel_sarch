package de.htwg.sa.kniffel.tui.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import de.htwg.sa.kniffel.tui.aview.TUI

import scala.concurrent.ExecutionContext

class TuiApi(using val tui: TUI):
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  Http().newServerAt("localhost", 9005).bind(
    pathPrefix("tui") {
      concat(
        put {
          concat(
            path("quit") {
              complete(tui.update("quit"))
            },
            path("save") {
              complete(tui.update("save"))
            },
            path("load") {
              complete(tui.update("load"))
            },
            path("move") {
              complete(tui.update("move"))
            },
          )
        }
      )
    }
  )