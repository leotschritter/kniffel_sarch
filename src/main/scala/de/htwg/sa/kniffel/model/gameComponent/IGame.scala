package de.htwg.sa.kniffel
package model.gameComponent

trait IGame {
  def next(): Option[IGame]

  def undoMove(value: Int, y: Int): IGame

  def sum(value: Int, y: Int): IGame

  def playerID: Int

  def playerName: String

  def playerName(x: Int): String

  def resultNestedList: List[List[Int]]

  def nestedList: List[List[Int]]

  def remainingMoves: Int

  def playerTuples: List[(Int, String)]
}