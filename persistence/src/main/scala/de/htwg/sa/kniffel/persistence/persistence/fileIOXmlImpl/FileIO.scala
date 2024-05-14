package de.htwg.sa.kniffel.persistence.persistence.fileIOXmlImpl

import de.htwg.sa.kniffel.persistence.persistence.IPersistence
import de.htwg.sa.kniffel.persistence.persistence.fileIOXmlImpl.converter.JsonToXmlConverter
import play.api.libs.json.{JsArray, JsBoolean, JsNumber, Json}

import java.io.{File, PrintWriter}
import scala.util.Try
import scala.xml.{Elem, NodeSeq, PrettyPrinter}

// @formatter:off
class FileIO(converter: JsonToXmlConverter) extends IPersistence {
  def this() = {
    this(new JsonToXmlConverter())
  }

  override def saveDiceCup(diceCup: String): String = {
    val pw = new PrintWriter(new File("dicecup.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(converter.diceCupToXml(diceCup))
    pw.write(xml)
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveGame(game: String): String = {
    val pw = new PrintWriter(new File("game.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(converter.gameToXml(game))
    pw.write(xml)
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveField(field: String): String = {
    val pw = new PrintWriter(new File("field.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(converter.fieldToXml(field))
    pw.write(xml)
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def loadDiceCup: String = {
    val file: Elem = scala.xml.XML.loadFile("dicecup.xml")
    val remainingDices: Int = (file \\ "dicecup" \ "@remainingDices").toString.toInt
    val locked: List[Int] = (file \\ "locked" \ "dice").map(_.text.trim.toInt).toList
    val inCup: List[Int] = (file \\ "incup" \ "dice").map(_.text.trim.toInt).toList

    Json.obj(
      "dicecup" -> Json.obj(
        "stored" -> locked,
        "incup" -> inCup,
        "remainingDices" -> JsNumber(remainingDices)
      )
    ).toString
  }

  override def loadGame: String = {
    val file: Elem = scala.xml.XML.loadFile("game.xml")
    val remainingMoves: Int = (file \\ "game" \ "@remainingMoves").text.trim.toInt
    val currentPlayerID: Int = (file \\ "game" \ "@currentPlayerID").text.trim.toInt
    val currentPlayerName: String = (file \\ "game" \ "@currentPlayerName").text.trim
    val playersList: JsArray = JsArray((file \\ "player").map { player =>
        Json.obj(
          "id" -> JsNumber((player \ "@playerid").text.trim.toInt),
          "name" -> (player \ "@playername").text.trim
        )
      })
    val total: Seq[Int] = (file \\ "total").map(_.text.trim.toInt)
    val bonus: Seq[Int] = (file \\ "bonus").map(_.text.trim.toInt)
    val total_of_upper_section: Seq[Int] = (file \\ "total_of_upper_section").map(_.text.trim.toInt)
    val total_of_lower_section: Seq[Int] = (file \\ "total_of_lower_section").map(_.text.trim.toInt)
    val grand_total: Seq[Int] = (file \\ "grand_total").map(_.text.trim.toInt)
    val resultNestedList: List[List[Int]] = total.indices.map { x =>
      List(total(x), bonus(x), total_of_upper_section(x), total_of_lower_section(x), total_of_upper_section(x), grand_total(x))
    }.toList

    Json.obj(
      "game" -> Json.obj(
        "nestedList" -> resultNestedList.map(_.mkString(",")).mkString(";"),
        "remainingMoves" -> JsNumber(remainingMoves),
        "currentPlayerID" -> JsNumber(currentPlayerID),
        "currentPlayerName" -> currentPlayerName,
        "players" -> playersList
      )
    ).toString
  }

  override def loadField: String = {
    val file: Elem = scala.xml.XML.loadFile("field.xml")
    val numberOfPlayers: Int = (file \\ "field" \ "@numberOfPlayers").text.trim.toInt
    val cellNodes: NodeSeq = file \\ "cell"
    val cells: Map[(Int, Int), Option[Int]] =
      cellNodes.map { cell =>
        val row = (cell \ "@row").text.trim.toInt
        val col = (cell \ "@col").text.trim.toInt
        val cellOption = Try(cell.text.trim.toInt).toOption
        (row, col) -> cellOption
      }.toMap

    val nestedVector: Vector[Vector[Option[Int]]] =
      (0 until 19).map { rows =>
        (0 until numberOfPlayers).map { cols =>
          cells((rows, cols))
        }.toVector
      }.toVector

    Json.obj(
      "field" -> Json.obj(
        "numberOfPlayers" -> JsNumber(numberOfPlayers),
        "rows" -> nestedVector
      )
    ).toString
  }

  override def createGame(numberOfPlayers:  Int): String = "XML FileIO does not implement this"
  
}
