package de.htwg.se.kniffel
package model.fieldComponent

trait IField {
  def putMulti(valueList: List[String], putInValue: String, x: Int, y: Int): IField

  def undoMove(valueList: List[String], x: Int, y: Int): IField

  def numberOfPlayers: Int

  def getMatrix: IMatrix
}

trait IMatrix {
  def cell(col: Int, row: Int): String

  def isEmpty(col: Int, row: Int): Boolean
}