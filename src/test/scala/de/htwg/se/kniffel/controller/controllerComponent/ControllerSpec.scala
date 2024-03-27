package de.htwg.se.kniffel
package controller.controllerComponent

import controller.controllerBaseImpl.Controller
import model.Move
import model.dicecupComponent.IDiceCup
import model.dicecupComponent.dicecupBaseImpl.DiceCup
import model.fieldComponent.fieldBaseImpl.Field
import model.gameComponent.gameBaseImpl.{Game, Player}
import util.{Event, Observer}
import Config.given


import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class ControllerSpec extends AnyWordSpec {
  "The Controller" should {
    val controller = Controller()
    val controller2 = Controller()

    "notify its observers on change" in {
      class TestObserver(controller: Controller) extends Observer :
        controller.add(this)
        var bing = false

        def update(e: Event): Unit = {
          bing = true
        }

      val testObserver = TestObserver(controller)
      testObserver.bing should be(false)
      controller.doAndPublish(controller.dice())
      testObserver.bing should be(true)
    }

    "dices are put out the Dice Cup or in" should {
      val sortOut: IDiceCup = controller.putOut(controller.diceCup.getInCup.take(2))
      "be inserted into the locked list of a new DiceCup Object" in {
        sortOut.getLocked.size should be(2)
        sortOut.getInCup.size should be(3)
      }
      "be inserted into the inCup list of a new DiceCup Object" in {
        val putIn: IDiceCup = controller.putIn(controller.diceCup.getLocked.take(2))
        putIn.getLocked.size should be(0)
        putIn.getInCup.size should be(5)
      }
    }

    "dices are thrown" should {
      "contain two lists with all dices" in {
        val thrownDiceCup: IDiceCup = controller.dice()
        thrownDiceCup.getInCup.size + thrownDiceCup.getLocked.size should be(5)
        thrownDiceCup.getInCup.foreach {
          s =>
            s should be < 7
            s should be > 0
        }
      }
    }

    "set a new Game object" when {
      "finishing a move" in {
        controller2.next()
        controller2.game should be (Game(List(Player(0,"Player 1"), Player(1,"Player 2")),Player(1,"Player 2"),25,List(List(0, 0, 0, 0, 0, 0), List(0, 0, 0, 0, 0, 0))))
      }
    }
    "after a Move" when {
      "write down the result" in {
        val con = controller.nextRound()
        con.getRemainingDices should be(2)
        con.getInCup.size should be(0)
        con.getLocked.size should be(0)
      }
    }
    "when toString is called" should {
      "toString" in {
        controller.toString should be(controller.field.toString)
      }
    }
    "when undo/redo/put/save/load is called" should {
      "put" in {
        controller.put(Move("11", 0, 0))
        controller.field.getMatrix.cell(0, 0) should be("11")
      }
      "undo" in {
        controller.undo()
        controller.field.getMatrix.cell(0, 0) should be("")
      }
      "redo" in {
        controller.redo()
        controller.field.getMatrix.cell(0, 0) should be("11")
      }
      "save and loaded" in {
        controller.diceCup = DiceCup(List(), List(1, 2, 3, 4, 5), 2)
        controller.save
        controller.load
        controller.diceCup should be (DiceCup(List(), List(1, 2, 3, 4, 5), 2))
      }
    }
    "when game quit" should {
      "end the game" in {
        val controller3: Controller = controller
        controller.quit().toString should be(controller3.quit().toString)
      }
    }
  }
}

