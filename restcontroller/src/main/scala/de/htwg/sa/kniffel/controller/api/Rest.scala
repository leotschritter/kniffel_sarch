package de.htwg.sa.kniffel.controller.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import de.htwg.sa.kniffel.controller.integration.gui.GuiESI
import de.htwg.sa.kniffel.controller.integration.tui.TuiESI
import de.htwg.sa.kniffel.controller.model.IController
import de.htwg.sa.kniffel.controller.util.Event.*
import de.htwg.sa.kniffel.controller.util.{Event, Move, Observer}

import java.net.{HttpURLConnection, URL}
import scala.concurrent.ExecutionContext

case class Rest(controller: IController, tuiESI: TuiESI, guiESI: GuiESI) extends Observer:
  def this(controller: IController) = this(controller, TuiESI(), GuiESI())
  controller.add(this)

  override def update(e: Event): Unit = e match
    case Event.Quit => guiESI.sendRequest("gui/quit"); tuiESI.sendRequest("tui/quit")
    case Event.Save => guiESI.sendRequest("gui/save"); tuiESI.sendRequest("tui/save")
    case Event.Load => guiESI.sendRequest("gui/load"); tuiESI.sendRequest("tui/load")
    case Event.Move => guiESI.sendRequest("gui/move"); tuiESI.sendRequest("tui/move")

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher


  private val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
    Some(str.split("=").tail.mkString(",").split(",").map(_.toInt).toList)
  }

  private val StringValue: PathMatcher1[String] = PathMatcher("""\w+""".r)

  Http().newServerAt("localhost", 9006).bind(
    concat(
      pathPrefix("controller") {
        concat(
          get {
            concat(
              pathSingleSlash {
                complete(controller.toString)
              },
              path("controller") {
                complete(controller.toJson.toString)
              },
              path("field") {
                complete(controller.field.toJson.toString)
              },
              path("game") {
                complete(controller.game.toJson.toString)
              },
              path("diceCup") {
                complete(controller.diceCup.toJson.toString)
              },
              path("load") {
                complete(controller.load())
              },
              path("next") {
                complete(controller.next())
              },
              pathPrefix("doAndPublish") {
                concat(
                  path("nextRound") {
                    complete(controller.doAndPublish(controller.nextRound()))
                  },
                  path("dice") {
                    val c = controller.doAndPublish(controller.dice())
                    print(c)
                    complete(c)
                  },
                  path("putIn" / IntList) {
                    (pi: List[Int]) =>
                      complete(controller.doAndPublish(controller.putIn(pi)))
                  },
                  path("putOut" / IntList) {
                    (po: List[Int]) =>
                      complete(controller.doAndPublish(controller.putOut(po)))
                  }
                )
              },
              path("save") {
                complete(controller.save())
              },
              path("undo") {
                complete(controller.undo())
              },
              path("redo") {
                complete(controller.redo())
              },
              path("writeDown" / StringValue) {
                (value: String) =>
                  try {
                    val currentPlayer = controller.gameESI.sendPlayerIDRequest(controller.game)
                    val indexOfField = controller.diceCupESI.sendIndexOfFieldRequest(value)
                    val result = controller.diceCupESI.sendResultRequest(indexOfField, controller.diceCup)
                    complete(controller.writeDown(Move(result, currentPlayer, indexOfField)))
                  } catch {
                    case e: Throwable => complete("Invalid Input!")
                  }
              },
              path("") {
                sys.error("No such GET route")
              }
            )
          },
          post {
            concat(
              path("put") {
                entity(as[String]) { requestBody =>
                  complete(controller.put(controller.jsonStringToMove(requestBody)))
                }
              },
              path("quit") {
                complete(controller.quit())
              },
              path("nextRound") {
                complete(controller.nextRound().toJson.toString)
              },
              path("") {
                sys.error("No such POST route")
              }
            )
          }
        )
      }
    )
  )
