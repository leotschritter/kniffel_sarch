package de.htwg.sa.kniffel.persistence.persistence.util

import play.api.libs.json.*

class JsonConverter {

  def diceCupToJsonString(remDice: Int, stored: List[Int], inCup: List[Int]): String = {
    Json.obj(
      "dicecup" -> Json.obj(
        "stored" -> stored,
        "incup" -> inCup,
        "remainingDices" -> JsNumber(remDice)
      )
    ).toString
  }

  def fieldToJsonString(numberOfPlayers: Int, resultMap: Vector[Vector[Option[Int]]]): String = {
    Json.obj(
      "field" -> Json.obj(
        "numberOfPlayers" -> JsNumber(numberOfPlayers),
        "rows" -> resultMap.map(row => JsArray(row.map {
          case Some(value) => JsNumber(value)
          case None => JsNull
        }))
      )
    ).toString
  }

  def gameToJsonString(remMoves: Int, resultTuples: List[(String, Boolean, Int, Int, Int, Int, Int)]): String = {
    val resultNestedList: List[List[Int]] = resultTuples
      .map(p => List(p._3, p._4, p._5, p._6, p._5, p._7))

    val currentPlayer: (String, Int) = resultTuples.find(_._2)
      .map(p => (p._1, p._1.substring(7).toIntOption.map(x => x - 1).getOrElse(1)))
      .getOrElse(("Player 1", 1))

    val playersList: JsArray = JsArray(resultTuples.map { p =>
      Json.obj(
        "id" -> JsNumber(p._1.substring(7).toIntOption.map(x => x - 1).getOrElse(1)),
        "name" -> p._1
      )
    })
    Json.obj(
      "game" -> Json.obj(
        "nestedList" -> resultNestedList.map(_.mkString(",")).mkString(";"),
        "remainingMoves" -> JsNumber(remMoves),
        "currentPlayerID" -> JsNumber(currentPlayer._2),
        "currentPlayerName" -> currentPlayer._1,
        "players" -> playersList
      )
    ).toString
  }
}
