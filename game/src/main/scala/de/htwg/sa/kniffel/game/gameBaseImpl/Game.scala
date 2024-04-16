package de.htwg.sa.kniffel.game.gameBaseImpl

import de.htwg.sa.kniffel.game.IGame

import scala.annotation.tailrec

case class Game(playersList: List[Player], currentPlayer: Player, remainingMoves: Int, resultNestedList: List[List[Int]]) extends IGame :
  def this(numberOfPlayers: Int) = this(
    (0 until numberOfPlayers).map(s => Player(s, "Player " + (s + 1))).toList,
    Player(0, "Player 1"),
    numberOfPlayers * 13,
    List.fill(numberOfPlayers, 6)(0)
  )

  def next(): Option[Game] = {
    if (remainingMoves == 0)
      None
    else
      Some(Game(playersList, nextPlayer, remainingMoves - 1, resultNestedList))
  }

  private def previousPlayer: Player = {
    if (playersList.indexOf(currentPlayer) - 1 < 0)
      playersList(playersList.last.playerID)
    else
      playersList(playersList.indexOf(currentPlayer) - 1)
  }


  private def nextPlayer: Player =
    playersList((playersList.indexOf(currentPlayer) + 1) % playersList.length)

  private def sums(value: Int, y: Int, player: Player): (Int, Int, Int) = {
    val sumTop: Int = if y < 6 then value + nestedList(playersList.indexOf(player)).head else
      nestedList(playersList.indexOf(player)).head
    val sumBottom: Int = if y > 8 then value + nestedList(playersList.indexOf(player))(3) else
      nestedList(playersList.indexOf(player))(3)
    val bonus: Int = if sumTop >= 63 then 35 else 0
    (sumTop, sumBottom, bonus)
  }

  def sum(value: Int, y: Int): Game = {
    val (sumTop, sumBottom, bonus) = sums(value, y, currentPlayer)
    Game(playersList, currentPlayer, remainingMoves, resultNestedList.updated(
      playersList.indexOf(currentPlayer),
      List(sumTop) :+ bonus :+ (sumTop + bonus) :+ sumBottom :+ (sumTop + bonus) :+ (sumBottom + sumTop + bonus)
    ))
  }

  def undoMove(value: Int, y: Int): Game = {
    val (sumTop, sumBottom, bonus) = sums(-value, y, previousPlayer)
    Game(playersList, previousPlayer, remainingMoves + 1, resultNestedList.updated(
      playersList.indexOf(previousPlayer),
      List(sumTop) :+ bonus :+ (sumTop + bonus) :+ sumBottom :+ (sumTop + bonus) :+ (sumBottom + sumTop + bonus)
    ))
  }


  def playerID: Int = currentPlayer.playerID

  def playerName: String = currentPlayer.playerName

  def playerName(x: Int): String = playersList(x).playerName

  def nestedList: List[List[Int]] = resultNestedList


  def playerTuples: List[(Int, String)] =
    @tailrec
    def playerTuplesHelper(players: List[Player], acc: List[(Int, String)]): List[(Int, String)] = players match
      case Nil => acc.reverse
      case player :: rest => playerTuplesHelper(rest, (player.playerID, player.playerName) :: acc)
    playerTuplesHelper(playersList, Nil)
