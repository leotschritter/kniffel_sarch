package de.htwg.sa.kniffel.controller.entity

case class Matrix[T](rows: Vector[Vector[Option[Int]]]):

  def this(cols: Int, row_s: Int = 19) = this(Vector.tabulate(row_s, cols) { (cols, row_s) => None })

  def cell(col: Int, row: Int): Option[Int] = rows(row)(col)

  def fill(col: Int, row: Int, value: Option[Int]): Matrix[T] = copy(rows.updated(row, rows(row).updated(col, value)))
  

