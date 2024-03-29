package de.htwg.sa.kniffel
package aview

import controller.controllerBaseImpl.Controller
import model.dicecupComponent.dicecupBaseImpl.DiceCup
import model.fieldComponent.fieldBaseImpl.Field
import model.gameComponent.gameBaseImpl.Game
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import model.Move
import Config.given

class UISpec extends AnyWordSpec {
  "An UI" when {
    val ui = TUI()
    "dices are put in or out" should {
      "put out or in" in {
        ui.diceCupPutOut(List())
        ui.getController.diceCup.locked should be(List())
        ui.diceCupPutIn(List())
        ui.getController.diceCup.locked should be(List())
      }
    }
    "the player writes down a number" should {
      ui.writeDown(Move("2", 0, 0))
      "set the number into the field and trigger a new round" in {
        ui.getController.diceCup.locked should be (List())
        ui.getController.diceCup.inCup should be (List())
        ui.getController.field.matrix.cell(0, 0) should be("2")
        ui.getController.field.matrix.cell(0, 6) should be ("2")
        ui.getController.field.matrix.cell(0, 7) should be ("0")
        ui.getController.field.matrix.cell(0, 8) should be ("2")
        ui.getController.field.matrix.cell(0, 16) should be ("0")
        ui.getController.field.matrix.cell(0, 17) should be ("2")
        ui.getController.field.matrix.cell(0, 18) should be ("2")
        ui.getController.game.playerID should be(1)
      }
    }
  }
}