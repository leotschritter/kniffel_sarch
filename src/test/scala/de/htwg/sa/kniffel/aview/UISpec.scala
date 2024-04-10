package de.htwg.sa.kniffel
package aview

import de.htwg.sa.kniffel.Config.given
import de.htwg.sa.kniffel.model.Move
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

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
      ui.writeDown(Move(2, 0, 0))
      "set the number into the field and trigger a new round" in {
        ui.getController.diceCup.locked should be (List())
        ui.getController.diceCup.inCup should be (List())
        ui.getController.field.matrix.cell(0, 0).get should be(2)
        ui.getController.field.matrix.cell(0, 6).get should be(2)
        ui.getController.field.matrix.cell(0, 7).get should be(0)
        ui.getController.field.matrix.cell(0, 8).get should be(2)
        ui.getController.field.matrix.cell(0, 16).get should be(0)
        ui.getController.field.matrix.cell(0, 17).get should be(2)
        ui.getController.field.matrix.cell(0, 18).get should be(2)
        ui.getController.game.playerID should be(1)
      }
    }
  }
}