package de.htwg.sa.kniffel.controller.entity

import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}

import scala.util.Random

case class DiceCup(locked: List[Int], inCup: List[Int], remDices: Int):
  def this() = this(List.fill(0)(0), List.fill(5)(Random.between(1, 7)), 2)

  def toJson: JsObject = {
    Json.obj(
      "dicecup" -> Json.obj(
        "stored" -> this.locked,
        "incup" -> this.inCup,
        "remainingDices" -> JsNumber(this.remainingDices)
      )
    )
  }
  
  def jsonStringToDiceCup(diceCup: String): DiceCup = {
    val json: JsValue = Json.parse(diceCup)
    val diceCupJson: JsValue = (json \ "dicecup").get
    val storedList: List[Int] = (diceCupJson \ "stored").get.as[List[Int]]
    val inCupList: List[Int] = (diceCupJson \ "incup").get.as[List[Int]]
    val remDice: Int = (diceCupJson \ "remainingDices").get.toString.toInt

    DiceCup(storedList, inCupList, remDice)
  }
  
  def remainingDices: Int = remDices
  
