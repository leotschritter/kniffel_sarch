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

import scala.annotation.tailrec


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

    @tailrec
    def createPlayers(index: Int, acc: List[Player], ids: Seq[Int], names: Seq[String]): List[Player] = {
      if (index >= ids.length)
        acc.reverse // Reverse the accumulated list since we're prepending players
      else {
        val id = ids(index)
        val name = names(index)
        createPlayers(index + 1, Player(id, name) :: acc, ids, names)
      }
    }

    val ids = (json \\ "id").map(x => x.as[Int])
    val names = (json \\ "name").map(x => x.as[String].replace("\"", ""))
    val playersList: List[Player] = createPlayers(0, Nil, ids.toList, names.toList) // Pass the ids and names as parameters

    val game: IGame = Game(playersList, Player(currentPlayerId, currentPlayerName), remainingMoves, nestedList)
    game
  }

  override def loadField: IField = {
    val bufferedSource: BufferedSource = Source.fromFile("field.json")
    val source: String = bufferedSource.getLines.mkString
    bufferedSource.close()
    val json: JsValue = Json.parse(source)
    val numberOfPlayers: Int = (json \ "field" \ "numberOfPlayers").get.toString.toInt

    @tailrec
    def updateMatrix(vector: Vector[Vector[String]], index: Int): Vector[Vector[String]] = {
      if (index >= 19 * numberOfPlayers)
        vector
      else {
        val row = (json \\ "row")(index).as[Int]
        val col = (json \\ "col")(index).as[Int]
        val cell = (json \\ "cell")(index).as[String]
        val updatedRow = vector(row).updated(col, cell)
        updateMatrix(vector.updated(row, updatedRow), index + 1)
      }
    }

    val initialMatrix: Vector[Vector[String]] = Vector.tabulate(19, numberOfPlayers) { (cols, row_s) => "" }
    val matrixVector: Vector[Vector[String]] = updateMatrix(initialMatrix, 0)

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
    def playersToJson(playerTuples: List[(Int, String)]): JsArray = {
      playerTuples match {
        case Nil => Json.arr()
        case (id, name) :: tail =>
          Json.arr(Json.obj("id" -> JsNumber(id), "name" -> name)) ++ playersToJson(tail)
      }
    }

    def nestedListToString(nestedList: List[List[Int]]): String = {
      nestedList match {
        case Nil => ""
        case innerList :: remainingLists =>
          innerList.mkString(",") + (if (remainingLists.nonEmpty) ";" else "") + nestedListToString(remainingLists)
      }
    }

    Json.obj(
      "game" -> Json.obj(
        "nestedList" -> nestedListToString(game.nestedList),
        "remainingMoves" -> JsNumber(game.remainingMoves),
        "currentPlayerID" -> JsNumber(game.playerID),
        "currentPlayerName" -> game.playerName,
        "players" -> playersToJson(game.playerTuples)
      )
    )
  }

  private def fieldToJson(field: IField, matrix: IMatrix): JsObject = {
    Json.obj(
      "field" -> Json.obj(
        "numberOfPlayers" -> JsNumber(field.numberOfPlayers),
        "cells" -> Json.toJson(
          (0 until field.numberOfPlayers) flatMap (col =>
            (0 until 19) map (row =>
              Json.obj(
                "row" -> row,
                "col" -> col,
                "cell" -> Json.toJson(matrix.cell(col, row))
              )
              )
            )
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

    def convertStringListToIntList(index: Int): List[List[Int]] = {
      if (index >= valueList.length)
        Nil
      else {
        val intList = valueList(index).split(",").map(_.toInt).toList
        intList :: convertStringListToIntList(index + 1)
      }
    }

    convertStringListToIntList(0)
  }
}
