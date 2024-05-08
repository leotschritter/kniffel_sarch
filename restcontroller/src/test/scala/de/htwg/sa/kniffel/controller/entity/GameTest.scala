package de.htwg.sa.kniffel.controller.entity

import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameTest extends AnyWordSpec with Matchers:
  "A Game" when {
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    val game = Option(Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0)))
    "created" should {
      "have a list of players" in {
        game.get.playersList should not be empty
      }
      "have the first player at first" in {
        game.get.currentPlayer should be(players.head)
        game.get.playerName(0) should be("Player1")
      }
    }
    "a nested list is requested" should {
      "return the nested list with the expected values" in {
        game.get.nestedList should be(game.get.resultNestedList)
      }
    }
    "when converted to JSON" should {
      "look like" in {
        val game: Game = new Game(2)
        game.toJson.toString should be(
          "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0," +
            "\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1," +
            "\"name\":\"Player 2\"}]}}"
        )
      }
    }
    "when converted from JSON" should {
      "look like" in {
        val game: Game = new Game(2)
        game.jsonStringToGame("{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26," +
          "\"currentPlayerID\":0,\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"}," +
          "{\"id\":1,\"name\":\"Player 2\"}]}}") should be(
          game)
      }
    }
  }

