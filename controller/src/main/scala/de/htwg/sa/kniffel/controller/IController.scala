package de.htwg.sa.kniffel
package controller

import akka.http.scaladsl.server.Route
import de.htwg.sa.kniffel.util.{Move, Observable}
import play.api.libs.json.JsObject

trait IController extends Observable {
  def undo(): IController

  def redo(): IController

  def put(move: Move): IController

  def quit(): IController

  def next(): IController

  def doAndPublish(doThis: List[Int] => String, list: List[Int]): IController

  def putOut(list: List[Int]): String

  def putIn(list: List[Int]): String

  def doAndPublish(doThis: => String): IController

  def dice(): String

  def nextRound(): String

  def toString: String

  def field: String

  def diceCup: String

  def game: String

  def save(): IController

  def load(): IController

  def toJson: JsObject

  val controllerRoute: Route
}
