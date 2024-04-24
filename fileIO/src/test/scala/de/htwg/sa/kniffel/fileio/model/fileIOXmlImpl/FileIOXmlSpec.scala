package de.htwg.sa.kniffel.fileio.model.fileIOXmlImpl

import de.htwg.sa.kniffel.fileio.fileIOXmlImpl.FileIO
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class FileIOXmlSpec extends AnyWordSpec {
  "A Game" when {
    val fileIO = new FileIO()
    val game = "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0," +
      "\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1," +
      "\"name\":\"Player 2\"}]}}"
    val field = "{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}"

    val diceCup = "{\"dicecup\":{\"stored\":[2,2],\"incup\":[2,2,2],\"remainingDices\":2}}"
    

    "saved to xml" should {
      "equal the previously saved game after loading" in {
        fileIO.saveGame(game)
        fileIO.loadGame should be(game)
      }
    }
    "A Field" when {
      "saved to xml" should {
        "equal the previously saved field after loading" in {

          fileIO.saveField(field)
          fileIO.loadField should be(field)
        }
      }
    }
    "A DiceCup" when {
      "saved to xml" should {
        "equal the previously saved DiceCup after loading" in {
          fileIO.saveDiceCup(diceCup)
          fileIO.loadDiceCup should be(diceCup)
        }
      }
    }
  }

}
