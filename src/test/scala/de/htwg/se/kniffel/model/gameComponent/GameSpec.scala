package de.htwg.se.kniffel
package model.gameComponent

import model.gameComponent.gameBaseImpl.{Game, Player}

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable

class GameSpec extends AnyWordSpec {
  "A Game" when {
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    val game = Option(Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0)))
    "created" should {
      "have a list of players" in {
        game.get.playersList should not be empty
      }
      "have the first player at first" in {
        game.get.currentPlayer should be(players.head)
        game.get.getPlayerName(0) should be("Player1")
      }
    }
    "move has ended" should {
      "get the next Player where the current player is not the last listElement" in {
        game.get.next().get.currentPlayer.playerName should be("Player2")
      }
    }
    "with no moves remaining next" should {
      "not build a new game" in {
        val players2: List[Player] = List(Player(0, "Player1"))
        var game2 = Option(Game(players2, players2.head, players2.length * 13, List.fill(players2.length, 6)(0)))

        for (a <- 1 to 13) {
          game2 = game2.get.next()
        }
        game2.get.remainingMoves should be(0)
        game2.get.next() should be(None)
      }
    }
    "after writing down a Move Game" should {
      "return a new Game" in {
        val players2: List[Player] = List(Player(0, "Player1"))
        val game2 = Option(Game(players2, players2.head, players2.length * 13, List.fill(players2.length, 6)(0)))
        game2.get.sum(62, 0) should be(Game(List(Player(0, "Player1")), Player(0, "Player1"), 13, List(List(62, 0, 62, 0, 62, 62))))
        game2.get.sum(63, 0) should be(Game(List(Player(0, "Player1")), Player(0, "Player1"), 13, List(List(63, 35, 98, 0, 98, 98))))
        game2.get.resultNestedList(game2.get.playersList.indexOf(game2.get.currentPlayer)) should be(List(0, 0, 0, 0, 0, 0))
      }
    }
    "a Game move is undon" should {
      "return different sums depending on the values" in {
        val players3: List[Player] = List(Player(0, "Player1"))
        val game3 = Option(Game(players3, players3.head, players3.length * 13, List.fill(players3.length, 6)(0)))
        val game4 = game3.get.sum(62, 9)
        game4.undoMove(62, 9) should be(Game(List(Player(0, "Player1")), Player(0, "Player1"), 14, List(List(0, 0, 0, 0, 0, 0))))
        val game5 = game4.sum(75, 0)
        val game6 = game5.sum(7, 1)
        game6.undoMove(7, 1) should be(Game(List(Player(0, "Player1")), Player(0, "Player1"), 14, List(List(75, 35, 110, 62, 110, 172))))
      }
    }
    "a nested list is requested" should {
      "return the nested list with the expected values" in {
        game.get.getNestedList should be (game.get.resultNestedList)
      }
    }
  }
}
