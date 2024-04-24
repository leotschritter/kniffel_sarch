package de.htwg.sa.kniffel.controller.entity

case class Matrix[T](rows: Vector[Vector[Option[Int]]]):

  def this(cols: Int, row_s: Int = 19) = this(Vector.tabulate(row_s, cols) { (cols, row_s) => None })

