package de.htwg.sa.kniffel.game.model

import akka.http.scaladsl.server.Route
import play.api.libs.json.JsObject

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

  def toJson: JsObject
    
  def jsonStringToGame(game: String): IGame
  
  def newGame(players: Int): IGame
  
}
