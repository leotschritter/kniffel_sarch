package de.htwg.sa.kniffel.fileio.fileIOXmlImpl

import de.htwg.sa.kniffel.fileio.IFileIO
import play.api.libs.json.*

import scala.util.Try
import scala.xml.{Elem, NodeSeq, PrettyPrinter}

// @formatter:off
class FileIO extends IFileIO {

  override def saveDiceCup(diceCup: String): String = {
    import java.io.*
    val pw = new PrintWriter(new File("dicecup.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(diceCupToXml(diceCup))
    pw.write(xml)
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveGame(game: String): String = {
    import java.io.*
    val pw = new PrintWriter(new File("game.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(gameToXml(game))
    pw.write(xml)
    pw.close()
    Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString
  }

  override def saveField(field: String): String = {
    import java.io.*
    val pw = new PrintWriter(new File("field.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(fieldToXml(field))
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

  private def fieldToXml(field: String): Elem = {
    val numberOfPlayers = (Json.parse(field) \ "field" \ "numberOfPlayers").as[Int]
    val rows = (Json.parse(field) \ "field" \ "rows").as[Array[JsValue]].map { outerValue =>
      outerValue.as[Array[JsValue]].map { innerVal =>
        innerVal.as[JsValue].match {
          case JsNumber(value) => Some(value.toInt)
          case _ => None
        }
      }.toList
    }.toList
    <field numberOfPlayers={numberOfPlayers.toString}>
      {(0 until numberOfPlayers).flatMap { col =>
      (0 until 19).map { row =>
        <cell row={row.toString} col={col.toString}>{rows(row)(col).map(cell => cell.toString).getOrElse("")}</cell>
      }
    }}</field>
  }

  private def diceCupToXml(diceCup: String): Elem = {
    val lockedList = (Json.parse(diceCup) \ "dicecup" \ "stored").as[List[Int]]
    val inCupList = (Json.parse(diceCup) \ "dicecup" \ "incup").as[List[Int]]
    val remainingDices = (Json.parse(diceCup) \ "dicecup" \ "remainingDices").as[Int]

    val lockedDiceElements = lockedList.map(dice => <dice>{dice}</dice>)
    val inCupDiceElements = inCupList.map(dice => <dice>{dice}</dice>)

    <dicecup remainingDices={remainingDices.toString}>
      <locked quantity={lockedDiceElements.length.toString}>{lockedDiceElements}</locked>
      <incup quantity={inCupDiceElements.length.toString}>{inCupDiceElements}</incup>
    </dicecup>
  }

  private def gameToXml(game: String): Elem = {
    val players = (Json.parse(game) \ "game" \ "players").as[Array[JsObject]]
    val nestedList = (Json.parse(game) \ "game" \ "nestedList").as[String].split(";").map(elem => elem.split(","))
    val remainingMoves = (Json.parse(game) \ "game" \ "remainingMoves").as[Int]
    val currentPlayerID = (Json.parse(game) \ "game" \ "currentPlayerID").as[Int]
    val currentPlayerName = (Json.parse(game) \ "game" \ "currentPlayerName").as[String]
    val playerElements = players.map { playerJson =>
      val playerID = (playerJson \ "id").as[Int].toString
      val playerName = (playerJson \ "name").as[String]
      <player playerid={playerID} playername={playerName}>
        <total>{nestedList(players.indexOf(playerJson)).head}</total>
        <bonus>{nestedList(players.indexOf(playerJson))(1)}</bonus>
        <total_of_upper_section>{nestedList(players.indexOf(playerJson))(2)}</total_of_upper_section>
        <total_of_lower_section>{nestedList(players.indexOf(playerJson))(3)}</total_of_lower_section>
        <grand_total>{nestedList(players.indexOf(playerJson)).last}</grand_total>
      </player>
    }

    <game remainingMoves={remainingMoves.toString} currentPlayerID={currentPlayerID.toString} currentPlayerName={currentPlayerName}>
      <scores>{playerElements}</scores>
    </game>
  }
}
