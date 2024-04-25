package de.htwg.sa.kniffel.controller.util

import de.htwg.sa.kniffel.controller.entity.{Field, Game, Player}
import de.htwg.sa.kniffel.controller.integration.field.FieldESI
import de.htwg.sa.kniffel.controller.integration.game.GameESI
import de.htwg.sa.kniffel.controller.model.controllerBaseImpl.SetCommand
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock

class UndoManagerSpec extends AnyWordSpec {
  "An UndoManager" should {
    val gameESI = mock[GameESI]
    val fieldESI = mock[FieldESI]
    val undoManager = new UndoManager[Game, Field]
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    var game = Option(Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0)))
    var field = new Field(4)

    "have a do, undo and redo" in {
      when(gameESI.sendRequest(any[String], any[String])).thenReturn(game.get)
      when(gameESI.sendNextRequest(any[Game])).thenReturn(game)
      when(fieldESI.sendRequest(any[String], any[String])).thenReturn(Field(field.matrix.fill(0, 0, Some(11))))
      var r = undoManager.doStep(game.get, field, SetCommand(Move(12, 0, 0), gameESI, fieldESI))
      var iGame: Game = r._1
      var iField: Field = r._2
      iGame.playerName should be("Player1")

      when(gameESI.sendRequest(any[String], any[String])).thenReturn(Game(players, players.last, players.length * 13, List.fill(players.length, 6)(0)))
      r = undoManager.undoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2

      iGame.playerName should be("Player4")

      when(gameESI.sendRequest(any[String], any[String])).thenReturn(game.get)
      r = undoManager.undoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame.playerName should be("Player1")

      when(gameESI.sendRequest(any[String], any[String])).thenReturn(Game(players, players(1), players.length * 13, List.fill(players.length, 6)(0)))
      when(gameESI.sendNextRequest(any[Game])).thenReturn(Some(Game(players, players(1), players.length * 13, List.fill(players.length, 6)(0))))

      r = undoManager.redoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame.playerName should be("Player2")

      when(gameESI.sendRequest(any[String], any[String])).thenReturn(game.get)
      r = undoManager.redoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame.playerName should be("Player1")
    }
  }
}
