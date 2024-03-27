package de.htwg.se.kniffel
package model.fieldComponent.fieldBaseImpl

import model.fieldComponent.IMatrix

case class Matrix[T](rows: Vector[Vector[String]]) extends IMatrix :
  def this(cols: Int, row_s: Int = 19) = this(Vector.tabulate(row_s, cols) { (cols, row_s) => "" })

  def cell(col: Int, row: Int): String = rows(row)(col)

  def fill(col: Int, row: Int, value: String): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, value)))

  def isEmpty(col: Int, row: Int): Boolean = cell(col, row).equals("")
