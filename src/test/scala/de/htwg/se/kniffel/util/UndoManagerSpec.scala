package de.htwg.se.kniffel
package util

import controller.controllerBaseImpl.SetCommand
import util.UndoManager
import model.fieldComponent.IField
import model.fieldComponent.fieldBaseImpl.Field
import model.gameComponent.gameBaseImpl.{Game, Player}
import model.gameComponent.IGame
import model.Move

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*

class UndoManagerSpec extends AnyWordSpec {
  "An UndoManager" should {
    val undoManager = new UndoManager[IGame, IField]
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    var game = Option(Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0)))
    var field = new Field(4)

    "have a do, undo and redo" in {
      var r = undoManager.doStep(game.get, field, SetCommand(Move("12", 0, 0)))
      var iGame:IGame = Option(r._1).get
      var iField:IField = r._2
      iGame.getPlayerName should be ("Player1")

      r = undoManager.undoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame.getPlayerName should be ("Player4")

      r = undoManager.undoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame.getPlayerName should be ("Player1")

      r = undoManager.redoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame.getPlayerName should be ("Player2")

      r = undoManager.redoStep(game.get, field)
      iGame = Option(r._1).get
      iField = r._2
      iGame.getPlayerName should be ("Player1")
    }
  }
}
