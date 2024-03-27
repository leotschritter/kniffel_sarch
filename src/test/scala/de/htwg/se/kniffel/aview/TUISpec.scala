package de.htwg.se.kniffel
package aview

import controller.controllerBaseImpl.Controller
import model.dicecupComponent.dicecupBaseImpl.DiceCup
import model.fieldComponent.fieldBaseImpl.Field
import model.gameComponent.gameBaseImpl.Game
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import model.Move
import Config.given


import scala.util.{Failure, Success, Try}

class TUISpec extends AnyWordSpec {
  "The TUI" should {
    val tui = TUI()


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
      val result = tui.invalidInput(List("wd"))
      result.isSuccess should be(false)
      val result2 = tui.invalidInput(List("wd", "1"))
      result2.isSuccess should be(true)
    }
  }
}
