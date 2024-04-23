package de.htwg.sa.kniffel.controller.controllerBaseImpl

import de.htwg.sa.kniffel.util.Move
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class SetCommandSpec extends AnyWordSpec {
  "The SetCommand" when {
    val setCommand = new SetCommand(Move(11, 0, 0))
    var game = "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0," +
      "\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1," +
      "\"name\":\"Player 2\"}]}}"
    var field = "{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]," +
      "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}"

    "it does a step" should {
      "be able to do a Step" in {
        val r = setCommand.doStep(game, field)
        val game2 = r._1
        val field2 = r._2
        field2 should be("{\"field\":{\"numberOfPlayers\":2,\"rows\":[[11,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[11,null],[0,null],[11,null],[null,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[null,null],[0,null],[11,null],[11,null]]}}")
      }
      "be able to undo the step and should not be able to more undo steps than there are on the undoStack" in {
        val r = setCommand.undoStep(game, field)
        val game3 = r._1
        val field3 = r._2
        field3 should be("{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[0,null],[0,null],[0,null],[null,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[null,null],[0,null],[0,null],[0,null]]}}")
        val r2 = setCommand.undoStep(game, field)
        val game4 = r2._1
        val field4 = r2._2
        field4 should be(field3)
      }
      "be able to redo the ste and should not be able to more redo steps than there are on the redoStack" in {
        val r = setCommand.redoStep(game, field)
        val game5 = r._1
        val field5 = r._2
        field5 should be("{\"field\":{\"numberOfPlayers\":2,\"rows\":[[11,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[11,null],[0,null],[11,null],[null,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[null,null],[0,null],[11,null],[11,null]]}}")
        val r2 = setCommand.redoStep(game, field)
        val game6 = r2._1
        val field6 = r2._2
        field6 should be(field5)
      }
      "be able to do a no step" in {
        val r = setCommand.noStep(game, field)
        val game7 = r._1
        val field7 = r._2
        field7 should be(field)
      }
    }
  }
}

