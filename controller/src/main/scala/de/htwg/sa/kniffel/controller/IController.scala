package de.htwg.sa.kniffel
package controller

import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.field.IField
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.util.{Move, Observable}
import play.api.libs.json.JsObject

trait IController extends Observable {
  def undo(): Unit

  def redo(): Unit

  def put(move: Move): Unit

  def quit(): Unit

  def next(): Unit

  def doAndPublish(doThis: List[Int] => IDiceCup, list: List[Int]): Unit

  def putOut(list: List[Int]): IDiceCup

  def putIn(list: List[Int]): IDiceCup

  def doAndPublish(doThis: => IDiceCup): Unit

  def dice(): IDiceCup

  def nextRound(): IDiceCup

  def toString: String

  def field: IField

  def diceCup: IDiceCup

  def game: IGame

  def save(): Unit

  def load(): Unit

  def toJson: JsObject
}
