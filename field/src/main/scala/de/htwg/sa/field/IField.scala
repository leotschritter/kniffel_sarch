package de.htwg.sa.field

trait IField {
  def putMulti(valueList: List[Int], putInValue: Option[Int], x: Int, y: Int): IField

  def undoMove(valueList: List[Int], x: Int, y: Int): IField

  def numberOfPlayers: Int

  def matrix: IMatrix
}

trait IMatrix {
  def cell(col: Int, row: Int): Option[Int]

  def isEmpty(col: Int, row: Int): Boolean
}