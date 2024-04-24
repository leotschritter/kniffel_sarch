package de.htwg.sa.kniffel.field.model.fieldBaseImpl

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.*
import de.htwg.sa.kniffel.field.model.IMatrix
import play.api.libs.json.{JsBoolean, JsNumber, JsObject, Json}

case class Matrix[T](rows: Vector[Vector[Option[Int]]]) extends IMatrix :
  def this(cols: Int, row_s: Int = 19) = this(Vector.tabulate(row_s, cols) { (cols, row_s) => None })

  def cell(col: Int, row: Int): Option[Int] = rows(row)(col)

  def fill(col: Int, row: Int, value: Option[Int]): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, value)))

  def isEmpty(col: Int, row: Int): Boolean = cell(col, row).isEmpty