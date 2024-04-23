/*package de.htwg.sa.kniffel.tui

import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.controller.controllerBaseImpl.Controller
import de.htwg.sa.kniffel.util.{Event, Move}
import org.scalatest.matchers.should.Matchers.{be, *}
import org.scalatest.wordspec.AnyWordSpec


class TUISpec extends AnyWordSpec {

  val controller: IController = Controller()
  val tui: TUI = TUI(controller)
  "The TUI" should {


    "recognize the input wd 22  as an invalid Input" in {
      tui.analyseInput("wd 22") should be(None)

    }
    "recognize the input o00 as move of stone O to field (0,0)" in {
      tui.analyseInput("po 1 2 3 4 5") should be(None)
    }
    "recognize any input as None" in {
      tui.analyseInput("pi 1 2 3 4 5") should be(None)
      tui.analyseInput("d") should be(None)
      tui.analyseInput("u") should be(None)
      tui.analyseInput("r") should be(None)
      tui.analyseInput("wd") should be(None)
      tui.analyseInput("s") should be(None)
      tui.analyseInput("l") should be(None)
      tui.analyseInput("käsekuchen") should be(None)
    }
    "dice the DiceCup when input is d" in {
      tui.analyseInput("wd käsekuchen") should be(None)
    }
    "quit" in {
      tui.analyseInput("q") should be(None)
    }
    "a list is validated" in {
      val result = tui.validInput(List("wd"))
      result.isSuccess should be(false)
      val result2 = tui.validInput(List("wd", "1"))
      result2.isSuccess should be(true)
    }
    "dices are put in or out" should {
      "put out or in" in {
        tui.diceCupPutOut(List())
        tui.getController.diceCup.locked should be(List())
        tui.diceCupPutIn(List())
        tui.getController.diceCup.locked should be(List())
      }
    }
    "the player writes down a number" should {
      tui.writeDown(Move(2, 0, 0))
      "set the number into the field and trigger a new round" in {
        tui.getController.diceCup.locked should be(List())
        tui.getController.diceCup.inCup should be(List())
        tui.getController.field.matrix.cell(0, 0).get should be(2)
        tui.getController.field.matrix.cell(0, 6).get should be(2)
        tui.getController.field.matrix.cell(0, 7).get should be(0)
        tui.getController.field.matrix.cell(0, 8).get should be(2)
        tui.getController.field.matrix.cell(0, 16).get should be(0)
        tui.getController.field.matrix.cell(0, 17).get should be(2)
        tui.getController.field.matrix.cell(0, 18).get should be(2)
        tui.getController.game.playerID should be(1)
      }
      "not be able to write down into the same field twice" in {
        val move: Option[Move] = tui.analyseInput("wd 1")
        move should not be (None)
        tui.writeDown(move.get)
        val move2: Move = tui.analyseInput("wd 1").getOrElse(Move(1, 0, 0))
        tui.writeDown(move2)
        tui.analyseInput("wd 1") should be(None)
      }
    }
    "set the continue variable to false on quit" in {
      tui.update(Event.Quit)
      tui.continue should be (false)
    }
  }
}
*/