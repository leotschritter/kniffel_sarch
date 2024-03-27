package de.htwg.se.kniffel
package controller.controllerComponent

import controller.controllerBaseImpl.SetCommand
import model.Move
import model.fieldComponent.IField
import model.fieldComponent.fieldBaseImpl.Field
import model.gameComponent.IGame
import model.gameComponent.gameBaseImpl.{Game, Player}
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class SetCommandSpec extends AnyWordSpec {
  "The SetCommand" when {
    val setCommand = new SetCommand(Move("11", 0, 0))
    val players: List[Player] = List(Player(0, "Player1"), Player(1, "Player2"), Player(2, "Player3"), Player(3, "Player4"))
    var game: IGame = Option(Game(players, players.head, players.length * 13, List.fill(players.length, 6)(0))).get
    var field: IField = new Field(4)
    "it does a step" should {
      "be able to do a Step" in {
        val r = setCommand.doStep(game, field)
        game = r._1
        field = r._2
        field.getMatrix.cell(0, 0) should be("11")
      }
      "be able to undo the step and should not be able to more undo steps than there are on the undoStack" in {
        val r = setCommand.undoStep(game, field)
        game = r._1
        field = r._2
        field.getMatrix.cell(0, 0) should be("")
        val r2 = setCommand.undoStep(game, field)
        game = r2._1
        field = r2._2
        field.getMatrix.cell(0, 0) should be("")
      }
      "be able to redo the ste and should not be able to more redo steps than there are on the redoStack" in {
        val r = setCommand.redoStep(game, field)
        game = r._1
        field = r._2
        field.getMatrix.cell(0, 0) should be("11")
        val r2 = setCommand.redoStep(game, field)
        game = r2._1
        field = r2._2
        field.getMatrix.cell(0, 0) should be("11")
      }
      "be able to do a no step" in {
        val r = setCommand.noStep(game, field)
        game = r._1
        field = r._2
        field.getMatrix.cell(0, 0) should be("11")
      }
    }
  }
}

