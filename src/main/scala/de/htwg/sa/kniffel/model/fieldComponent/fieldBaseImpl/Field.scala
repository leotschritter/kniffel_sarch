package de.htwg.sa.kniffel
package model.fieldComponent.fieldBaseImpl

import model.fieldComponent.IField

import scala.annotation.tailrec


case class Field(matrix: Matrix[String]) extends IField:
  def this(numberOfPlayers: Int) = this(new Matrix[String](numberOfPlayers))

  private val defaultPlayers: Int = matrix.rows.flatten.length / 19

  private val first_column: List[String] =
    List("1", "2", "3", "4", "5", "6", "G", "B", "O", "3x", "4x", "FH", "KS", "GS", "KN", "CH", "U", "O", "E")

  def cells(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers, desc: String = "", v: List[String] = List.fill(defaultPlayers)("")): String =
    def generateBars(strings: List[String]): String = strings match
      case Nil => ""
      case head :: tail => "|" + head.padTo(cellWidth, ' ') + generateBars(tail)
    "|" + desc.padTo(cellWidth, ' ') + generateBars(v) + "|" + '\n'

  def bar(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): String = (("+" + "-" * cellWidth)
    * (numberOfPlayers + 1)) + "+" + '\n'

  def header(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): List[String] =
    def buildPlayerHeaders(n: Int): List[String] =
      if (n <= numberOfPlayers)
        ("|" + ("P" + n).padTo(cellWidth, ' ')) :: buildPlayerHeaders(n + 1)
      else
        Nil
    (" " * (cellWidth + 1)) :: buildPlayerHeaders(1)

  def mesh(cellWidth: Int = 3, numberOfPlayers: Int = defaultPlayers): String =
    @tailrec
    def buildMeshString(index: Int, acc: List[String]): List[String] =
      if (index <= 18)
        val rowCells = cells(
          cellWidth,
          numberOfPlayers,
          first_column.apply(index),
          matrix.rows.toList.flatten.slice(index * numberOfPlayers, (index + 1) * numberOfPlayers)
        )
        val rowString = bar(cellWidth) + rowCells
        buildMeshString(index + 1, acc :+ rowString)
      else
        acc

    val headerString = header() :+ "\n"
    (headerString ++ buildMeshString(0, List.empty) :+ bar(cellWidth)).mkString("")


  def undoMove(valueList: List[String], x: Int, y: Int): Field = putMulti(valueList, "", x, y)

  def putMulti(valueList: List[String], putInValue: String, x: Int, y: Int): Field = {
    val indexList: List[Int] = List(6, 7, 8, 16, 17, 18)
    this.copy(matrix.fill(x, indexList.head, valueList.head).fill(x, indexList(1), valueList(1))
      .fill(x, indexList(2), valueList(2)).fill(x, indexList(3), valueList(3))
      .fill(x, indexList(4), valueList(4)).fill(x, indexList.last, valueList.last).fill(x, y, putInValue))
  }

  def numberOfPlayers: Int = defaultPlayers

  def getMatrix: Matrix[String] = matrix

  override def toString: String = mesh()
