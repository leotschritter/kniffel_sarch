package de.htwg.sa.kniffel.field.fieldBaseImpl

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.*
import de.htwg.sa.kniffel.field.IMatrix
import play.api.libs.json.{JsBoolean, JsNumber, JsObject, Json}

case class Matrix[T](rows: Vector[Vector[Option[Int]]]) extends IMatrix :
  def this(cols: Int, row_s: Int = 19) = this(Vector.tabulate(row_s, cols) { (cols, row_s) => None })

  def cell(col: Int, row: Int): Option[Int] = rows(row)(col)

  def fill(col: Int, row: Int, value: Option[Int]): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, value)))

  def isEmpty(col: Int, row: Int): Boolean = cell(col, row).isEmpty

  override val matrixRoute: Route =
    get {
      concat(
        path("cell" / IntNumber / IntNumber) { (col: Int, row: Int) =>
          complete(
            cell(col, row).match {
              case Some(value) => Json.obj("value" -> JsNumber(value)).toString
              case None => "{}"
            }
          )
        },
        path("isEmpty" / IntNumber / IntNumber) { (col: Int, row: Int) =>
          complete(Json.obj("isEmpty" -> JsBoolean(this.isEmpty(col, row))).toString)
        },
        path("") {
          sys.error("No such GET route")
        }
      )
    }