package de.htwg.sa.kniffel.fileio.fileIOJsonImpl

import de.htwg.sa.kniffel.fileio.IFileIO
import play.api.libs.json.*

import scala.Predef.ArrowAssoc
import scala.io.{BufferedSource, Source}


class FileIO extends IFileIO {

  override def saveGame(game: String): String = {
    import java.io.*
    val pw = new PrintWriter(new File("game.json"))
    pw.write(Json.prettyPrint(Json.parse(game)))
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveField(field: String): String = {
    import java.io.*
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(Json.parse(field)))
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveDiceCup(diceCup: String): String = {
    import java.io.*
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
}
