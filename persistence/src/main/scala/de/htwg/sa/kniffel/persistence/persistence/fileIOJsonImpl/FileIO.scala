package de.htwg.sa.kniffel.persistence.persistence.fileIOJsonImpl

import de.htwg.sa.kniffel.persistence.persistence.IPersistence
import play.api.libs.json.{JsBoolean, Json}

import java.io.{File, PrintWriter}
import scala.io.{BufferedSource, Source}

class FileIO extends IPersistence {

  override def saveGame(game: String): String = {
    val pw = new PrintWriter(new File("game.json"))
    pw.write(Json.prettyPrint(Json.parse(game)))
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveField(field: String): String = {
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(Json.parse(field)))
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveDiceCup(diceCup: String): String = {
    val pw = new PrintWriter(new File("dicecup.json"))
    pw.write(Json.prettyPrint(Json.parse(diceCup)))
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def loadGame: String = {
    val bufferedSource: BufferedSource = Source.fromFile("game.json")
    val source: String = bufferedSource.getLines.mkString
    bufferedSource.close()
    source.replaceAll("\\s", "")
  }

  override def loadField: String = {
    val bufferedSource: BufferedSource = Source.fromFile("field.json")
    val source: String = bufferedSource.getLines.mkString
    bufferedSource.close()
    source.replaceAll("\\s", "")
  }

  override def loadDiceCup: String = {
    val bufferedSource: BufferedSource = Source.fromFile("dicecup.json")
    val source: String = bufferedSource.getLines.mkString
    bufferedSource.close()
    source.replaceAll("\\s", "")
  }

  override def loadDiceCup(gameId: Int): String = loadDiceCup

  override def loadField(gameId: Int): String = loadField
  
  override def loadGame(gameId: Int): String = loadGame

  override def createGame(numberOfPlayers: Int): String = "JSON FileIO does not implement this"
  
  override def loadOptions: String = "1"

  override def deleteGame(gameId: Int): Unit = ???

  override def deleteField(gameId: Int): Unit = ???

  override def deleteInCup(gameId: Int): Unit = ???

  override def deleteStoredDice(gameId: Int): Unit = ???

  override def updateGame(game: String, gameId: Int): Unit = ???

  override def updateField(field: String, gameId: Int): Unit = ???

  override def updateDiceCup(diceCup: String, gameId: Int): Unit = ???
}
