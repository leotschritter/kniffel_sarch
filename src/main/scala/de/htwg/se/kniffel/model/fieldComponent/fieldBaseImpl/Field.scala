package de.htwg.se.kniffel
package model.fieldComponent.fieldBaseImpl

import model.fieldComponent.IField



case class Field(matrix: Matrix[String]) extends IField :
  def this(numberOfPlayers: Int) = this(new Matrix[String](numberOfPlayers))

  val defaultPlayers: Int = matrix.rows.flatten.length / 19

  val first_column: List[String] =
    List("1", "2", "3", "4", "5", "6", "G", "B", "O", "3x", "4x", "FH", "KS", "GS", "KN", "CH", "U", "O", "E")

  def cells(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers, desc: String = "", v: List[String] = List.fill(defaultPlayers)("")): String =
    "|" + desc.padTo(cellWidth, ' ') + (for (s <- v) yield "|" + s.padTo(cellWidth, ' ')).mkString("") + "|" + '\n'

  def bar(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): String = (("+" + "-" * cellWidth)
    * (numberOfPlayers + 1)) + "+" + '\n'

  def header(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): List[String] =
    (" " * (cellWidth + 1)) :: (for (n <- List.range(1, numberOfPlayers + 1)) yield "|"
      + ("P" + n).padTo(cellWidth, ' '))

  def mesh(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): String =
    (header() :+ "\n" :+ (for (s <- 0 to 18) yield bar(cellWidth)
      + cells(cellWidth, numberOfPlayers, first_column.apply(s), matrix.rows.toList.flatten.slice(
      0 + s * numberOfPlayers, s * numberOfPlayers + numberOfPlayers
    ))).mkString("") :+ bar(cellWidth)).mkString("")

  def undoMove(valueList: List[String], x: Int, y: Int): Field = putMulti(valueList, "", x, y)

  def putMulti(valueList: List[String], putInValue: String, x: Int, y: Int): Field = {
    val indexList: List[Int] = List(6, 7, 8, 16, 17, 18)
    this.copy(matrix.fill(x, indexList.head, valueList.head).fill(x, indexList(1), valueList(1))
      .fill(x, indexList(2), valueList(2)).fill(x, indexList(3), valueList(3))
      .fill(x, indexList(4), valueList(4)).fill(x, indexList.last, valueList.last).fill(x, y, putInValue))
  }

  def numberOfPlayers: Int = defaultPlayers

  def getMatrix: Matrix[String] = matrix

  override def toString = mesh()
