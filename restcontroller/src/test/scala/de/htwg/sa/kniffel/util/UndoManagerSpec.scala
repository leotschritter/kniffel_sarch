package de.htwg.sa.kniffel.util

import de.htwg.sa.kniffel.controller.controllerBaseImpl.SetCommand
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class UndoManagerSpec extends AnyWordSpec {
  "An UndoManager" should {
    val undoManager = new UndoManager[String, String]
    val game = "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0," +
      "\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1," +
      "\"name\":\"Player 2\"}]}}"
    val field = "{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}"


    "have a do, undo and redo" in {
      var r = undoManager.doStep(game, field, SetCommand(Move(12, 0, 0)))
      var iGame = r._1
      var iField = r._2
      iGame should be("{\"game\":{\"nestedList\":\"12,0,12,0,12,12;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0," +
        "\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}")

      r = undoManager.undoStep(game, field)
      iGame = r._1
      iField = r._2
      iGame should be("{\"game\":{\"nestedList\":\"0,0,0,0,0,0;-12,0,-12,0,-12,-12\",\"remainingMoves\":27,\"currentPlayerID\":1," +
        "\"currentPlayerName\":\"Player 2\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}")
      r = undoManager.undoStep(game, field)
      iGame = r._1
      iField = r._2
      iGame should be(game)

      r = undoManager.redoStep(game, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame should be("{\"game\":{\"nestedList\":\"12,0,12,0,12,12;0,0,0,0,0,0\",\"remainingMoves\":25,\"currentPlayerID\":1," +
        "\"currentPlayerName\":\"Player 2\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}")


      r = undoManager.redoStep(game, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame should be(game)
    }
  }
}
