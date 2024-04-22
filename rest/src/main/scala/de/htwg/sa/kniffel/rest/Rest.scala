package de.htwg.sa.kniffel.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.field.fieldBaseImpl.Field
import de.htwg.sa.kniffel.game.gameBaseImpl.Game
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
// import de.htwg.sa.kniffel.fileio.fileIOJsonImpl.FileIO
import de.htwg.sa.kniffel.fileio.fileIOXmlImpl.FileIO
import de.htwg.sa.kniffel.util.{Event, Observer}

import scala.concurrent.{ExecutionContext, Future}

case class Rest(controller: IController) extends Observer {
  controller.add(this)
  override def update(e: Event): Unit = println("update(e: Event) not implemented in Rest.scala")

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher
  val field = new Field(2)
  val game = new Game(2)
  val diceCup = new DiceCup()
  val fileIOJson = new FileIO

  val bindingFuture: Future[Http.ServerBinding] = Http().newServerAt("localhost", 8080).bind(
    concat(
      pathPrefix("controller") {
        this.controller.controllerRoute
      },
      pathPrefix("game") {
        this.game.gameRoute
      },
      pathPrefix("diceCup") {
        this.diceCup.diceCupRoute
      },
      pathPrefix("field") {
        this.field.fieldRoute
      },
      pathPrefix("io") {
        this.fileIOJson.fileIORoute
      }
    )
  )
}