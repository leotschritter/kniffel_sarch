package de.htwg.se.kniffel
package model.gameComponent.gameBaseImpl

import model.gameComponent.IGame

case class Game(playersList: List[Player], currentPlayer: Player, remainingMoves: Int, resultNestedList: List[List[Int]]) extends IGame :
  def this(numberOfPlayers: Int)
  = this((for (s <- 0 until numberOfPlayers) yield Player(s, "Player " + (s + 1))).toList,
    Player(0, "Player 1"),
    numberOfPlayers * 13,
    List.fill(numberOfPlayers, 6)(0))

  def next(): Option[Game] = {
    if (remainingMoves == 0)
      None
    else
      Some(Game(playersList, getNextPlayer, remainingMoves - 1, resultNestedList))
  }

  def getPreviousPlayer: Player = {
    if (playersList.indexOf(currentPlayer) - 1 < 0)
      playersList(playersList.last.playerID)
    else
      playersList(playersList.indexOf(currentPlayer) - 1)
  }


  def getNextPlayer: Player =
    playersList((playersList.indexOf(currentPlayer) + 1) % playersList.length)

  def getSums(value: Int, y: Int, player: Player): (Int, Int, Int) = {
    val sumTop: Int = if y < 6 then value + resultNestedList(playersList.indexOf(player)).head else
      resultNestedList(playersList.indexOf(player)).head
    val sumBottom: Int = if y > 8 then value + resultNestedList(playersList.indexOf(player))(3) else
      resultNestedList(playersList.indexOf(player))(3)
    val bonus: Int = if sumTop >= 63 then 35 else 0
    (sumTop, sumBottom, bonus)
  }

  def sum(value: Int, y: Int): Game = {
    val (sumTop, sumBottom, bonus) = getSums(value, y, currentPlayer)
    Game(playersList, currentPlayer, remainingMoves, resultNestedList.updated(
      playersList.indexOf(currentPlayer),
      List(sumTop) :+ bonus :+ (sumTop + bonus) :+ sumBottom :+ (sumTop + bonus) :+ (sumBottom + sumTop + bonus)
    ))
  }

  def undoMove(value: Int, y: Int): Game = {
    val (sumTop, sumBottom, bonus) = getSums(-value, y, getPreviousPlayer)
    Game(playersList, getPreviousPlayer, remainingMoves + 1, resultNestedList.updated(
      playersList.indexOf(getPreviousPlayer),
      List(sumTop) :+ bonus :+ (sumTop + bonus) :+ sumBottom :+ (sumTop + bonus) :+ (sumBottom + sumTop + bonus)
    ))
  }

  // override def toString: String = currentPlayer.playerID.toString + " " + currentPlayer.playerName + "\n" + playersList + "\n" + (for(x <- playersList.indices) yield resultNestedList(x)).mkString + "\n" + remainingMoves

  def getPlayerID: Int = currentPlayer.playerID

  def getPlayerName: String = currentPlayer.playerName

  def getPlayerName(x: Int): String = playersList(x).playerName

  def getResultNestedList(x: Int): List[Int] = resultNestedList(x)

  def getNestedList: List[List[Int]] = resultNestedList

  def getRemainingMoves: Int = remainingMoves

  def getPlayerTuples: List[(Int, String)] = for(x <- playersList) yield (x.playerID, x.playerName)
