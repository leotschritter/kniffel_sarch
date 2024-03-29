package de.htwg.sa.kniffel
package model.fileIOComponent.fileIOJsonImpl

import model.dicecupComponent.dicecupBaseImpl.DiceCup
import model.fieldComponent.fieldBaseImpl.{Field, Matrix}
import model.gameComponent.gameBaseImpl.{Game, Player}

import scala.io.{BufferedSource, Source}
import model.fileIOComponent.IFileIO
import model.dicecupComponent.IDiceCup
import model.fieldComponent.IField
import model.fieldComponent.IMatrix
import model.gameComponent.IGame
import play.api.libs.json.*

import javax.management.ValueExp


class FileIO extends IFileIO {

  override def saveGame(game: IGame): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("game.json"))
    pw.write(Json.prettyPrint(gameToJson(game)))
    pw.close()
  }

  override def saveField(field: IField, matrix: IMatrix): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(fieldToJson(field, matrix)))
    pw.close()
  }

  override def saveDiceCup(diceCup: IDiceCup): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("dicecup.json"))
    pw.write(Json.prettyPrint(diceCupToJson(diceCup)))
    pw.close()
  }

  override def loadGame: IGame = {

    val bufferedSource: BufferedSource = Source.fromFile("game.json")
    val source: String = bufferedSource.getLines.mkString
    bufferedSource.close()
    val json: JsValue = Json.parse(source)
    val remainingMoves: Int = (json \ "game" \ "remainingMoves").get.toString.toInt
    val currentPlayerId: Int = (json \ "game" \ "currentPlayerID").get.toString.toInt
    val currentPlayerName: String = (json \ "game" \ "currentPlayerName").get.toString.replace("\"", "")
    val nestedList: List[List[Int]] = nestedListGame((json \ "game" \ "nestedList").get.toString.replace("\"", ""))
    val ids = (json \\ "id").map(x => x.as[Int])
    val names = (json \\ "name").map(x => x.as[String].replace("\"", ""))
    val playersList: List[Player] = (for (x <- ids.indices) yield Player(ids(x), names(x))).toList
    val game: IGame = Game(playersList, Player(currentPlayerId, currentPlayerName), remainingMoves, nestedList)
    game
  }

  override def loadField: IField = {
    val bufferedSource: BufferedSource = Source.fromFile("field.json")
    val source: String = bufferedSource.getLines.mkString
    bufferedSource.close()
    val json: JsValue = Json.parse(source)
    val numberOfPlayers: Int = (json \ "field" \ "numberOfPlayers").get.toString.toInt
    var matrixVector = Vector.tabulate(19, numberOfPlayers) { (cols, row_s) => "" }
    for (index <- 0 until 19 * numberOfPlayers) {
      val row = (json \\ "row") (index).as[Int]
      val col = (json \\ "col") (index).as[Int]
      val cell = (json \\ "cell") (index).as[String]
      matrixVector = matrixVector.updated(row, matrixVector(row).updated(col, cell))
    }
    val field: IField = Field(Matrix(matrixVector))
    field
  }

  override def loadDiceCup: IDiceCup = {
    val bufferedSource: BufferedSource = Source.fromFile("dicecup.json")
    val source: String = bufferedSource.getLines.mkString
    bufferedSource.close()
    val json: JsValue = Json.parse(source)
    val remainingDices: Int = (json \ "dicecup" \ "remaining-dices").get.toString.toInt
    val locked: List[Int] = if ((json \ "dicecup" \ "locked").get.toString.replace("\"", "").isEmpty) List[Int]() else (json \ "dicecup" \ "locked").get.toString.replace("\"", "").split(",").map(_.toInt).toList
    val incup: List[Int] = if ((json \ "dicecup" \ "incup").get.toString.replace("\"", "").isEmpty) List[Int]() else (json \ "dicecup" \ "incup").get.toString.replace("\"", "").split(",").map(_.toInt).toList
    val diceCup: IDiceCup = DiceCup(locked, incup, remainingDices)
    println(diceCup)
    diceCup
  }

  private def gameToJson(game: IGame): JsObject = {
    Json.obj(
      "game" -> Json.obj(
        "nestedList" -> game.nestedList.map(_.mkString(",")).mkString(";"),
        "remainingMoves" -> JsNumber(game.remainingMoves),
        "currentPlayerID" -> JsNumber(game.playerID),
        "currentPlayerName" -> game.playerName,
        "players" -> Json.toJson(
          Seq(for {
            x <- game.playerTuples
          } yield {
            Json.obj(
              "id" -> JsNumber(x._1),
              "name" -> x._2)
          })
        )
      )
    )
  }

  private def fieldToJson(field: IField, matrix: IMatrix): JsObject = {
    Json.obj(
      "field" -> Json.obj(
        "numberOfPlayers" -> JsNumber(field.numberOfPlayers),
        "cells" -> Json.toJson(
          for {
            col <- 0 until field.numberOfPlayers
            row <- 0 until 19
          } yield {
            Json.obj(
              "row" -> row,
              "col" -> col,
              "cell" -> Json.toJson(matrix.cell(col, row))
            )
          }
        )
      )
    )
  }

  private def diceCupToJson(diceCup: IDiceCup): JsObject = {
    Json.obj(
      "dicecup" -> Json.obj(
        "locked" -> diceCup.locked.mkString(","),
        "incup" -> diceCup.inCup.mkString(","),
        "remaining-dices" -> JsNumber(diceCup.remainingDices)
      )
    )
  }

  def nestedListGame(values: String): List[List[Int]] = {
    val valueList: List[String] = values.split(";").toList
    (for (x <- valueList.indices) yield valueList(x).split(",").map(_.toInt).toList).toList
  }
}
