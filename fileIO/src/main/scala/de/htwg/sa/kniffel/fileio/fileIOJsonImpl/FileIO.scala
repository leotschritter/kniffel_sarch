package de.htwg.sa.kniffel.fileio.fileIOJsonImpl


import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.field.fieldBaseImpl.{Field, Matrix}
import de.htwg.sa.kniffel.field.{IField, IMatrix}
import de.htwg.sa.kniffel.fileio.IFileIO
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.game.gameBaseImpl.{Game, Player}
import play.api.libs.json.*

import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}
import scala.util.Try


class FileIO extends IFileIO {

  override def saveGame(game: IGame): Unit = {
    import java.io.*
    val pw = new PrintWriter(new File("game.json"))
    pw.write(Json.prettyPrint(gameToJson(game)))
    pw.close()
  }

  override def saveField(field: IField, matrix: IMatrix): Unit = {
    import java.io.*
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(fieldToJson(field, matrix)))
    pw.close()
  }

  override def saveDiceCup(diceCup: IDiceCup): Unit = {
    import java.io.*
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
    def updateMatrix(vector: Vector[Vector[Option[Int]]], index: Int): Vector[Vector[Option[Int]]] = {
      if (index >= 19 * numberOfPlayers)
        vector
      else {
        val row = (json \\ "row")(index).as[Int]
        val col = (json \\ "col")(index).as[Int]
        val cell: Option[Int] = Try((json \\ "cell")(index).as[String].toInt).toOption
        val updatedRow = vector(row).updated(col, cell)
        updateMatrix(vector.updated(row, updatedRow), index + 1)
      }
    }

    val initialMatrix: Vector[Vector[Option[Int]]] = Vector.tabulate(19, numberOfPlayers) { (cols, row_s) => None }
    val matrixVector: Vector[Vector[Option[Int]]] = updateMatrix(initialMatrix, 0)

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

  import play.api.libs.json.*

  private def gameToJson(game: IGame): JsObject = {
    @tailrec
    def playersToJson(playerTuples: List[(Int, String)], acc: JsArray = Json.arr()): JsArray = {
      playerTuples match {
        case Nil => acc
        case (id, name) :: tail =>
          val playerJson = Json.obj("id" -> JsNumber(id), "name" -> name)
          playersToJson(tail, acc :+ playerJson)
      }
    }

    @tailrec
    def nestedListToString(nestedList: List[List[Int]], acc: String = ""): String = {
      nestedList match {
        case Nil => acc
        case innerList :: remainingLists =>
          val innerString = innerList.mkString(",")
          val separator = if (remainingLists.nonEmpty) ";" else ""
          nestedListToString(remainingLists, acc + innerString + separator)
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
                "cell" -> Json.toJson(matrix.cell(col, row).map(cell => cell.toString).getOrElse(""))
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

    @tailrec
    def convertStringListToIntList(index: Int, acc: List[List[Int]] = Nil): List[List[Int]] = {
      if (index >= valueList.length)
        acc.reverse
      else {
        val intList = valueList(index).split(",").map(_.toInt).toList
        convertStringListToIntList(index + 1, intList :: acc)
      }
    }

    convertStringListToIntList(0)
  }
}
