package controllers

import akka.actor.ActorSystem

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RestartController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem) extends AbstractController(cc) {

  def restart(): Action[AnyContent] = Action {
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, Duration.Inf)
    Ok("Application restarted successfully.")
  }
}