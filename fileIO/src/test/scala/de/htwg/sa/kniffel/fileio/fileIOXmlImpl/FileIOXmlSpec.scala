package de.htwg.sa.kniffel.fileio.fileIOXmlImpl

import de.htwg.sa.kniffel.field.fieldBaseImpl.Field
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.game.gameBaseImpl.{Game, Player}
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class FileIOXmlSpec extends AnyWordSpec {
  "A Game" when {
    val fileIO = new FileIO()
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    val game = Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0))
    "saved to xml" should {
      "equal the previously saved game after loading" in {
        fileIO.saveGame(game)
        fileIO.loadGame should be(game)
      }
    }
    "A Field" when {
      val field: Field = new Field(2)
      "saved to xml" should {
        "equal the previously saved field after loading" in {

          fileIO.saveField(field, field.matrix)
          fileIO.loadField should be(field)
        }
      }
    }
    "A DiceCup" when {
      val diceCup: DiceCup = DiceCup(List(1, 1, 1, 1), List(1), 1)
      "saved to xml" should {
        "equal the previously saved DiceCup after loading" in {
          fileIO.saveDiceCup(diceCup)
          fileIO.loadDiceCup should be(diceCup)
        }
      }
    }
  }

}
