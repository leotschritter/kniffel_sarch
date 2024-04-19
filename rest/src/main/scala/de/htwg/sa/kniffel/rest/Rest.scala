package de.htwg.sa.kniffel.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.util.{Event, Observer}

import scala.concurrent.{ExecutionContext, Future}

case class Rest(controller: IController) extends Observer {
  controller.add(this)
  override def update(e: Event): Unit = println("update(e: Event) not implemented in Rest.scala")

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val bindingFuture: Future[Http.ServerBinding] = Http().newServerAt("localhost", 8080).bind(
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
        concat(
          this.controller.field.fieldRoute,
          this.controller.field.matrix.matrixRoute
        )
      }
    )
  )
}