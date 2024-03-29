package de.htwg.sa.kniffel
package model.fileIOComponent.fileIOXmlImpl

import model.dicecupComponent.dicecupBaseImpl.DiceCup
import model.gameComponent.gameBaseImpl.{Game, Player}
import model.dicecupComponent.IDiceCup
import model.fieldComponent.fieldBaseImpl.{Field, Matrix}
import model.fieldComponent.{IField, IMatrix}
import model.gameComponent.IGame
import model.fileIOComponent.IFileIO
import scala.xml.{Elem, NodeSeq, PrettyPrinter}

class FileIO extends IFileIO {

  override def saveDiceCup(diceCup: IDiceCup): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("dicecup.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(diceCupToXml(diceCup))
    pw.write(xml)
    pw.close()
  }

  override def saveGame(game: IGame): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("game.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(gameToXml(game))
    pw.write(xml)
    pw.close()
  }

  override def saveField(field: IField, matrix: IMatrix): Unit = {
    import java.io._
    val pw = new PrintWriter(new File("field.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(fieldToXml(field, matrix))
    pw.write(xml)
    pw.close()
  }

  override def loadDiceCup: IDiceCup = {
    val file: Elem = scala.xml.XML.loadFile("dicecup.xml")
    val remainingDices: Int = (file \\ "dicecup" \ "@remainingDices").toString.toInt
    val locked: List[Int] = (for f <- file \\ "locked" \ "dice" yield f.text.trim.toInt).toList
    val inCup: List[Int] = (for f <- file \\ "incup" \ "dice" yield f.text.trim.toInt).toList
    DiceCup(locked, inCup, remainingDices)
  }

  override def loadGame: IGame = {
    val file: Elem = scala.xml.XML.loadFile("game.xml")
    val remainingMoves: Int = (file \\ "game" \ "@remainingMoves").toString.toInt
    val currentPlayer: Player = Player((file \\ "game" \ "@currentPlayerID").toString.toInt, (file \\ "game" \ "@currentPlayerName").toString)
    val playersList: List[Player] = (for player <- file \\ "player" yield Player((player \ "@playerid").toString.toInt, (player \ "@playername").toString)).toList
    val total: Seq[Int] = for f <- file \\ "total" yield f.text.trim.toInt
    val bonus: Seq[Int] = for f <- file \\ "bonus" yield f.text.trim.toInt
    val total_of_upper_section: Seq[Int] = for f <- file \\ "total_of_upper_section" yield f.text.trim.toInt
    val total_of_lower_section: Seq[Int] = for f <- file \\ "total_of_lower_section" yield f.text.trim.toInt
    val grand_total: Seq[Int] = for f <- file \\ "grand_total" yield f.text.trim.toInt
    val resultNestedList: List[List[Int]] =
      (for x <- total.indices
        yield List(total(x), bonus(x), total_of_upper_section(x),
          total_of_lower_section(x), total_of_upper_section(x), grand_total(x))).toList
    Game(playersList, currentPlayer, remainingMoves, resultNestedList)
  }

  override def loadField: IField = {
    val file: Elem = scala.xml.XML.loadFile("field.xml")
    val numberOfPlayers: Int = (file \\ "field" \ "@numberOfPlayers").toString.toInt
    val cellNodes: NodeSeq = file \\ "cell"
    val cells: Map[(Int, Int), String] =
      (for (cell <- cellNodes)
        yield (((cell \ "@row").toString.toInt, (cell \ "@col").toString.toInt), cell.text.trim)).toMap[(Int, Int), String]
    val nestedVector: Vector[Vector[String]] =
      (for (rows <- 0 until 19) yield (for (cols <- 0 until numberOfPlayers) yield cells((rows, cols))).toVector).toVector
    Field(Matrix(nestedVector))
  }

  def fieldToXml(field: IField, matrix: IMatrix): Elem = {
    <field numberOfPlayers={field.numberOfPlayers.toString}>
      {for {
      col <- 0 until field.numberOfPlayers
      row <- 0 until 19
    } yield {
      <cell row={row.toString} col={col.toString}>
        {matrix.cell(col, row)}
      </cell>
    }}
    </field>
  }

  def diceCupToXml(diceCup: IDiceCup): Elem = {
    <dicecup remainingDices={diceCup.remainingDices.toString}>
      <locked quantity={diceCup.locked.length.toString}>
        {for {
        index <- diceCup.locked.indices
      } yield {
        <dice>
          {diceCup.locked(index)}
        </dice>
      }}
      </locked>
      <incup quantity={diceCup.inCup.length.toString}>
        {for {
        index <- diceCup.inCup.indices
      } yield {
        <dice>
          {diceCup.inCup(index)}
        </dice>
      }}
      </incup>
    </dicecup>
  }

  def gameToXml(game: IGame): Elem = {
    <game remainingMoves={game.remainingMoves.toString} currentPlayerID={game.playerID.toString} currentPlayerName={game.playerName}>
      <scores>
        {for {
        col <- game.playerTuples.indices
      } yield {
        <player playerid={game.playerTuples(col)._1.toString} playername={game.playerTuples(col)._2}>
          <total>
            {game.resultNestedList(col).head}
          </total>
          <bonus>
            {game.resultNestedList(col)(1)}
          </bonus>
          <total_of_upper_section>
            {game.resultNestedList(col)(2)}
          </total_of_upper_section>
          <total_of_lower_section>
            {game.resultNestedList(col)(3)}
          </total_of_lower_section>
          <grand_total>
            {game.resultNestedList(col).last}
          </grand_total>
        </player>
      }}
      </scores>
    </game>
  }
}
