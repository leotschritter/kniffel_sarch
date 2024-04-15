package de.htwg.sa.kniffel
package controller.controllerComponent

import de.htwg.sa.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.Config.given
import de.htwg.sa.kniffel.controller.controllerBaseImpl.Controller
import de.htwg.sa.kniffel.model.Move
import de.htwg.sa.kniffel.model.gameComponent.gameBaseImpl.{Game, Player}
import de.htwg.sa.kniffel.util.{Event, Observer}
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
      val sortOut: IDiceCup = controller.putOut(controller.diceCup.inCup.take(2))
      "be inserted into the locked list of a new DiceCup Object" in {
        sortOut.locked.size should be(2)
        sortOut.inCup.size should be(3)
      }
      "be inserted into the inCup list of a new DiceCup Object" in {
        val putIn: IDiceCup = controller.putIn(controller.diceCup.locked.take(2))
        putIn.locked.size should be(0)
        putIn.inCup.size should be(5)
      }
    }

    "dices are thrown" should {
      "contain two lists with all dices" in {
        val thrownDiceCup: IDiceCup = controller.dice()
        thrownDiceCup.inCup.size + thrownDiceCup.locked.size should be(5)
        thrownDiceCup.inCup.foreach {
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
        con.remainingDices should be(2)
        con.inCup.size should be(0)
        con.locked.size should be(0)
      }
    }
    "when toString is called" should {
      "toString" in {
        controller.toString should be(controller.field.toString)
      }
    }
    "when undo/redo/put/save/load is called" should {
      "put" in {
        controller.put(Move(11, 0, 0))
        controller.field.matrix.cell(0, 0).get should be(11)
      }
      "undo" in {
        controller.undo()
        controller.field.matrix.cell(0, 0) should be(None)
      }
      "redo" in {
        controller.redo()
        controller.field.matrix.cell(0, 0).get should be(11)
      }
      "save and loaded" in {
        controller.diceCup = DiceCup(List(), List(1, 2, 3, 4, 5), 2)
        controller.save()
        controller.load()
        controller.diceCup should be (DiceCup(List(), List(1, 2, 3, 4, 5), 2))
      }
    }
    "when game quit" should {
      "end the game" in {
        val controller3: Controller = controller
        controller.quit().toString should be(controller3.quit().toString)
      }
    }
    "when no remaining moves are left game.next" should {
      "be None" in {
        val game: Game = Game(List(Player(1, "Harald"), Player(2, "Dieter")), Player(1, "Harald"), 0, List())
        controller.game = game
        controller.next()
        controller.game should be(game)
      }
    }
  }
}

