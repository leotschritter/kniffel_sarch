package de.htwg.sa.kniffel
package model.fileIOComponent

import model.dicecupComponent.dicecupBaseImpl.DiceCup
import model.fieldComponent.fieldBaseImpl.{Field, Matrix}
import model.gameComponent.gameBaseImpl.{Game, Player}
import model.fileIOComponent.fileIOJsonImpl.FileIO
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class FileIOJsonSpec extends AnyWordSpec {
  "A Json IO" when {
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    val game = Option(Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0)))
    val field = new Field(4)
    val matrix = new Matrix(4)
    val diceCup1 = new DiceCup().dice()
    val diceCup2 = DiceCup(List.fill(5)(2), List(), 2)

    val fileio:FileIO = new FileIO()
    "loaded after save" should {
      "be the same game" in {
        fileio.saveGame(game.get)
        fileio.loadGame should be (game.get)
      }
      "be the same field" in {
        fileio.saveField(field, matrix)
        fileio.loadField should be (field)
      }
      "be the same diceCup" in {
        fileio.saveDiceCup(diceCup1)
        fileio.loadDiceCup should be (diceCup1)
        fileio.saveDiceCup(diceCup2)
        fileio.loadDiceCup should be (diceCup2)
      }
    }
    "helper method getNestedListGame() is called" should {
      "have a nested list matching the following pattern" in {
        fileio.getNestedListGame("23,44,12,23,97,123;12,69,67,26,23,32;12,21,87,32,12,213;92,23,123,12,23,200") should be (List(List(23, 44, 12, 23, 97, 123), List(12, 69, 67, 26, 23, 32), List(12, 21, 87, 32, 12, 213), List(92, 23, 123, 12, 23, 200)))
      }
    }
  }
}
