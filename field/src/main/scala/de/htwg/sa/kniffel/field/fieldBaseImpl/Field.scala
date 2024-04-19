package de.htwg.sa.kniffel.field.fieldBaseImpl


import de.htwg.sa.kniffel.field.IField
import play.api.libs.json.{JsNumber, JsObject, Json}

import scala.annotation.tailrec


case class Field(matrix: Matrix[Option[Int]]) extends IField:
  def this(numberOfPlayers: Int) = this(new Matrix[Option[Int]](numberOfPlayers))

  private val defaultPlayers: Int = matrix.rows.flatten.length / 19

  private val first_column: List[String] =
    List("1", "2", "3", "4", "5", "6", "G", "B", "O", "3x", "4x", "FH", "KS", "GS", "KN", "CH", "U", "O", "E")

  def cells(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers, desc: String = "", v: List[String] = List.fill(defaultPlayers)("")): String =
    @tailrec
    def generateBars(strings: List[String], acc: String): String = strings match
      case Nil => acc
      case head :: tail => generateBars(tail, acc + "|" + head.padTo(cellWidth, ' '))

    "|" + desc.padTo(cellWidth, ' ') + generateBars(v, "") + "|" + '\n'

  def bar(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): String = (("+" + "-" * cellWidth)
    * (numberOfPlayers + 1)) + "+" + '\n'

  def header(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): List[String] =
    @tailrec
    def buildPlayerHeaders(n: Int, acc: List[String]): List[String] =
      if (n <= numberOfPlayers)
        buildPlayerHeaders(n + 1, acc :+ ("|" + ("P" + n).padTo(cellWidth, ' ')))
      else
        acc

    (" " * (cellWidth + 1)) :: buildPlayerHeaders(1, Nil)


  def mesh(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): String =
    @tailrec
    def buildMeshString(index: Int, acc: List[String]): List[String] =
      if (index <= 18)
        val rowCells = cells(
          cellWidth,
          numberOfPlayers,
          first_column.apply(index),
          matrix.rows.toList.flatten.slice(index * numberOfPlayers, (index + 1) * numberOfPlayers)
            map (cell => cell.map(cellV => cellV.toString).getOrElse(""))
          
        )
        val rowString = bar(cellWidth) + rowCells
        buildMeshString(index + 1, acc :+ rowString)
      else
        acc

    val headerString = header() :+ "\n"
    (headerString ++ buildMeshString(0, List.empty) :+ bar(cellWidth)).mkString("")


  def undoMove(valueList: List[Int], x: Int, y: Int): Field = putMulti(valueList, None, x, y)

  def putMulti(valueList: List[Int], putInValue: Option[Int], x: Int, y: Int): Field = {
    val indexList: List[Int] = List(6, 7, 8, 16, 17, 18)
    this.copy(matrix.fill(x, indexList.head, Some(valueList.head)).fill(x, indexList(1), Some(valueList(1)))
      .fill(x, indexList(2), Some(valueList(2))).fill(x, indexList(3), Some(valueList(3)))
      .fill(x, indexList(4), Some(valueList(4))).fill(x, indexList.last, Some(valueList.last)).fill(x, y, putInValue))
  }

  def numberOfPlayers: Int = defaultPlayers

  override def toString: String = mesh()

  override def toJson: JsObject = {
    Json.obj(
      "field" -> Json.obj(
        "numberOfPlayers" -> JsNumber(this.numberOfPlayers),
        "rows" -> this.matrix.rows
      )
    )
  }

  import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}

  override val fieldRoute: Route =
    import akka.http.scaladsl.server.Directives.*
    val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
      val ints = str.split("=").tail.mkString(",").split(",").map(_.toInt)
      Some(ints.toList)
    }
    concat(
      get {
        concat(
          path("numberOfPlayers") {
            complete(Json.obj("numberOfPlayers" -> JsNumber(this.numberOfPlayers)).toString)
          },
          path("matrix") {
            complete(Json.obj("rows" -> this.matrix.rows).toString)
          },
          path("") {
            sys.error("No such GET route")
          }
        )
      },
      post {
        concat(
          // example: putMulti/list=1,0,1,0,1,1/1/0/0
          // curl -X POST http://localhost:8080/field/putMulti/list=1,0,1,0,1,1/1/0/0
          path("putMulti" / IntList / IntNumber / IntNumber / IntNumber) {
            (valueList: List[Int], putInValue: Int, x: Int, y: Int) =>
              complete(this.putMulti(valueList, Some(putInValue), x, y).toJson.toString)
          },
          // example: undoMove/list=1,0,1,0,1,1/0/0
          // curl -X POST http://localhost:8080/field/undoMove/list=1,0,1,0,1,1/0/0
          path("undoMove" / IntList / IntNumber / IntNumber) {
            (valueList: List[Int], x: Int, y: Int) =>
              complete(this.undoMove(valueList, x, y).toJson.toString)
          },
          path("") {
            sys.error("No such POST route")
          }
        )
      }
    )
