package de.htwg.sa.kniffel.persistence.persistence.fileIOJsonImpl

import de.htwg.sa.kniffel.persistence.persistence.fileIOJsonImpl.FileIO
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class FileIOJsonSpec extends AnyWordSpec {
  "A Json IO" when {
    val game = "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0," +
      "\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1," +
      "\"name\":\"Player 2\"}]}}"
    val field = "{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}"

    val diceCup1 = "{\"dicecup\":{\"stored\":[2,2],\"incup\":[2,2,2],\"remainingDices\":2}}"
    val diceCup2 = "{\"dicecup\":{\"stored\":[1,2,3],\"incup\":[2,2],\"remainingDices\":1}}"

    val fileio: FileIO = new FileIO()
    "loaded after save" should {
      "be the same game" in {
        fileio.saveGame(game)
        fileio.loadGame should be(game.replaceAll(" ", ""))
      }
      "be the same field" in {
        fileio.saveField(field)
        fileio.loadField should be(field)
      }
      "be the same diceCup" in {
        fileio.saveDiceCup(diceCup1)
        fileio.loadDiceCup.replace(" ", "") should be(diceCup1)
        fileio.saveDiceCup(diceCup2)
        fileio.loadDiceCup.replace(" ", "") should be(diceCup2)
      }
    }
  }
}
