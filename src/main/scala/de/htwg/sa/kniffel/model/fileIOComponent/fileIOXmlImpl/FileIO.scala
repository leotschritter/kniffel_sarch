package de.htwg.sa.kniffel
package model.fileIOComponent.fileIOXmlImpl

import de.htwg.sa.dicecup.IDiceCup
import de.htwg.sa.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.model.fieldComponent.fieldBaseImpl.{Field, Matrix}
import de.htwg.sa.kniffel.model.fieldComponent.{IField, IMatrix}
import de.htwg.sa.kniffel.model.fileIOComponent.IFileIO
import de.htwg.sa.kniffel.model.gameComponent.IGame
import de.htwg.sa.kniffel.model.gameComponent.gameBaseImpl.{Game, Player}

import scala.util.Try
import scala.xml.{Elem, NodeSeq, PrettyPrinter}

// @formatter:off
class FileIO extends IFileIO {

  override def saveDiceCup(diceCup: IDiceCup): Unit = {
    import java.io.*
    val pw = new PrintWriter(new File("dicecup.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(diceCupToXml(diceCup))
    pw.write(xml)
    pw.close()
  }

  override def saveGame(game: IGame): Unit = {
    import java.io.*
    val pw = new PrintWriter(new File("game.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(gameToXml(game))
    pw.write(xml)
    pw.close()
  }

  override def saveField(field: IField, matrix: IMatrix): Unit = {
    import java.io.*
    val pw = new PrintWriter(new File("field.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(fieldToXml(field, matrix))
    pw.write(xml)
    pw.close()
  }

  override def loadDiceCup: IDiceCup = {
    val file: Elem = scala.xml.XML.loadFile("dicecup.xml")
    val remainingDices: Int = (file \\ "dicecup" \ "@remainingDices").toString.toInt
    val locked: List[Int] = (file \\ "locked" \ "dice").map(_.text.trim.toInt).toList
    val inCup: List[Int] = (file \\ "incup" \ "dice").map(_.text.trim.toInt).toList
    DiceCup(locked, inCup, remainingDices)
  }

  override def loadGame: IGame = {
    val file: Elem = scala.xml.XML.loadFile("game.xml")
    val remainingMoves: Int = (file \\ "game" \ "@remainingMoves").text.trim.toInt
    val currentPlayer: Player = Player(
      (file \\ "game" \ "@currentPlayerID").text.trim.toInt,
      (file \\ "game" \ "@currentPlayerName").text.trim
    )
    val playersList: List[Player] = (file \\ "player").map { player =>
      Player(
        (player \ "@playerid").text.trim.toInt,
        (player \ "@playername").text.trim
      )
    }.toList
    val total: Seq[Int] = (file \\ "total").map(_.text.trim.toInt)
    val bonus: Seq[Int] = (file \\ "bonus").map(_.text.trim.toInt)
    val total_of_upper_section: Seq[Int] = (file \\ "total_of_upper_section").map(_.text.trim.toInt)
    val total_of_lower_section: Seq[Int] = (file \\ "total_of_lower_section").map(_.text.trim.toInt)
    val grand_total: Seq[Int] = (file \\ "grand_total").map(_.text.trim.toInt)
    val resultNestedList: List[List[Int]] = total.indices.map { x =>
      List(total(x), bonus(x), total_of_upper_section(x), total_of_lower_section(x), total_of_upper_section(x), grand_total(x))
    }.toList
    Game(playersList, currentPlayer, remainingMoves, resultNestedList)
  }

  override def loadField: IField = {
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
    Field(Matrix(nestedVector))
  }

  private def fieldToXml(field: IField, matrix: IMatrix): Elem = {
    <field numberOfPlayers={field.numberOfPlayers.toString}>
      {(0 until field.numberOfPlayers).flatMap { col =>
      (0 until 19).map { row =>
        <cell row={row.toString} col={col.toString}>{matrix.cell(col, row).map(cell => cell.toString).getOrElse("")}</cell>
      }
    }}</field>
  }

  private def diceCupToXml(diceCup: IDiceCup): Elem = {
    val lockedDiceElements = diceCup.locked.map(dice => <dice>{dice}</dice>)
    val inCupDiceElements = diceCup.inCup.map(dice => <dice>{dice}</dice>)

    <dicecup remainingDices={diceCup.remainingDices.toString}>
      <locked quantity={lockedDiceElements.length.toString}>{lockedDiceElements}</locked>
      <incup quantity={inCupDiceElements.length.toString}>{inCupDiceElements}</incup>
    </dicecup>
  }

  private def gameToXml(game: IGame): Elem = {
    val playerElements = game.playerTuples.indices.map { col =>
      val player = game.playerTuples(col)
      <player playerid={player._1.toString} playername={player._2}>
        <total>{game.resultNestedList(col).head}</total>
        <bonus>{game.resultNestedList(col)(1)}</bonus>
        <total_of_upper_section>{game.resultNestedList(col)(2)}</total_of_upper_section>
        <total_of_lower_section>{game.resultNestedList(col)(3)}</total_of_lower_section>
        <grand_total>{game.resultNestedList(col).last}</grand_total>
      </player>
    }

    <game remainingMoves={game.remainingMoves.toString} currentPlayerID={game.playerID.toString} currentPlayerName={game.playerName}>
      <scores>{playerElements}</scores>
    </game>
  }
}
