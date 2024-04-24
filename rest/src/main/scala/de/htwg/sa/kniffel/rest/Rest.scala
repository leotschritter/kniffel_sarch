package de.htwg.sa.kniffel.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.fileio.fileIOJsonImpl.FileIO
import de.htwg.sa.kniffel.gui.GUI
import de.htwg.sa.kniffel.tui.TUI
import de.htwg.sa.kniffel.util.Event.{Load, Move, Quit, Save}

import java.net.{HttpURLConnection, URL}
//import de.htwg.sa.kniffel.fileio.fileIOXmlImpl.FileIO
import de.htwg.sa.kniffel.util.{Event, Observer}

import scala.concurrent.ExecutionContext

case class Rest(controller: IController) extends Observer {
  controller.add(this)

  override def update(e: Event): Unit = e match
    case Quit => sendRequest("gui/quit"); sendRequest("tui/quit")
    case Save => sendRequest("gui/save"); sendRequest("tui/save")
    case Load => sendRequest("gui/load"); sendRequest("tui/load")
    case Move => sendRequest("gui/move"); sendRequest("tui/move")

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val fileIOJson = new FileIO

  Http().newServerAt("localhost", 8080).bind(
    concat(
      pathPrefix("controller") {
        this.controller.controllerRoute
      },
      pathPrefix("game") {
        this.controller.game.gameRoute
      },
      pathPrefix("diceCup") {
        this.controller.diceCup.diceCupRoute
      },
      pathPrefix("field") {
        this.controller.field.fieldRoute
      },
      pathPrefix("io") {
        this.fileIOJson.fileIORoute
      }
    )
  )

  // setup GUI and TUI Route
  val gui = new GUI
  val tui = new TUI
  Http().newServerAt("localhost", 24000).bind(
    concat(
      pathPrefix("gui") {
        this.gui.guiRoute
      },
      pathPrefix("tui") {
        this.tui.tuiRoute
      }
    )
  )
  tui.run()

  private def sendRequest(route: String): Unit = {
    val baseURL = "http://localhost:24000/"
    val url = new URL(baseURL + route)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    connection.setRequestMethod("PUT")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    if (connection.getResponseCode != HttpURLConnection.HTTP_OK) {
      throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode)
    }
  }
}