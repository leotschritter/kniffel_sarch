package de.htwg.sa.kniffel
package controller

import akka.http.scaladsl.server.Route
import de.htwg.sa.kniffel.util.{Move, Observable}
import play.api.libs.json.JsObject

trait IController extends Observable {
  def undo(): String

  def redo(): String

  def put(move: Move): String

  def quit(): String

  def next(): String

  def doAndPublish(doThis: List[Int] => String, list: List[Int]): String

  def putOut(list: List[Int]): String

  def putIn(list: List[Int]): String

  def doAndPublish(doThis: => String): String

  def dice(): String

  def nextRound(): String

  def toString: String

  def field: String

  def diceCup: String

  def game: String

  def save(): String

  def load(): String

  def toJson: JsObject

  val controllerRoute: Route
}
