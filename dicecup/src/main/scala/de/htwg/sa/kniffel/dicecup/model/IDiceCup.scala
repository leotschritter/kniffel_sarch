package de.htwg.sa.kniffel.dicecup.model

import akka.http.scaladsl.server.Route
import play.api.libs.json.JsObject

import scala.collection.immutable.ListMap

trait IDiceCup {
  def nextRound(): IDiceCup

  def inCup: List[Int]

  def locked: List[Int]

  def remainingDices: Int

  def result(index: Int): Int

  def putDicesIn(list: List[Int]): IDiceCup

  def putDicesOut(list: List[Int]): IDiceCup

  def dice(): Option[IDiceCup]

  def indexOfField: ListMap[String, Int]

  def toJson: JsObject

  def jsonStringToDiceCup(diceCup: String): IDiceCup
}
