package de.htwg.sa.kniffel.controller.model.controllerBaseImpl

import de.htwg.sa.kniffel.controller.entity.{Field, Game, Player}
import de.htwg.sa.kniffel.controller.integration.field.FieldESI
import de.htwg.sa.kniffel.controller.integration.game.GameESI
import de.htwg.sa.kniffel.controller.util.Move
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock

class SetCommandSpec extends AnyWordSpec {
  "The SetCommand" when {
    val gameESI = mock[GameESI]
    val fieldESI = mock[FieldESI]
    val setCommand = SetCommand(Move(11, 0, 0), gameESI, fieldESI)
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    var game: Game = Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0))
    var field: Field = new Field(4)
    val field2 = Field(field.matrix.fill(0, 0, Some(11)))
    "it does a step" should {
      "be able to do a Step" in {
        when(gameESI.sendRequest(any[String], any[String])).thenReturn(game)
        when(fieldESI.sendRequest(any[String], any[String])).thenReturn(field2)
        val r = setCommand.doStep(game, field)
        game = r._1
        field = r._2
        field.matrix.cell(0, 0).get should be(11)
      }
      "be able to undo the step and should not be able to more undo steps than there are on the undoStack" in {
        when(gameESI.sendRequest(any[String], any[String])).thenReturn(game)
        when(fieldESI.sendRequest(any[String], any[String])).thenReturn(Field(field.matrix.fill(0, 0, None)))
        val r = setCommand.undoStep(game, field)
        game = r._1
        field = r._2
        field.matrix.cell(0, 0) should be(None)
        val r2 = setCommand.undoStep(game, field)
        game = r2._1
        field = r2._2
        field.matrix.cell(0, 0) should be(None)
      }
      "be able to redo the ste and should not be able to more redo steps than there are on the redoStack" in {
        when(gameESI.sendRequest(any[String], any[String])).thenReturn(game)
        when(gameESI.sendNextRequest(any[Game])).thenReturn(Some(game))
        when(fieldESI.sendRequest(any[String], any[String])).thenReturn(Field(field.matrix.fill(0, 0, Some(11))))
        val r = setCommand.redoStep(game, field)
        game = r._1
        field = r._2
        field.matrix.cell(0, 0).get should be(11)
        val r2 = setCommand.redoStep(game, field)
        game = r2._1
        field = r2._2
        field.matrix.cell(0, 0).get should be(11)
      }
      "be able to do a no step" in {
        val r = setCommand.noStep(game, field)
        game = r._1
        field = r._2
        field.matrix.cell(0, 0).get should be(11)
      }
    }
  }
}

