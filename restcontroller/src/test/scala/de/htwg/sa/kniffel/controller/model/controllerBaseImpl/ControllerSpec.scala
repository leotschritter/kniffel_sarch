package de.htwg.sa.kniffel.controller.model.controllerBaseImpl

import de.htwg.sa.kniffel.controller.entity.*
import de.htwg.sa.kniffel.controller.integration.dicecup.DiceCupESI
import de.htwg.sa.kniffel.controller.integration.field.FieldESI
import de.htwg.sa.kniffel.controller.integration.fileio.FileIOESI
import de.htwg.sa.kniffel.controller.integration.game.GameESI
import de.htwg.sa.kniffel.controller.util.{Event, Move, Observer}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, when}
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock


class ControllerSpec extends AnyWordSpec {
  "The Controller" should {

    val diceCupESI = mock[DiceCupESI]
    val fieldESI = mock[FieldESI]
    val gameESI = mock[GameESI]
    val fileIOESI = mock[FileIOESI]

    val controller = Controller(new Field(2), new DiceCup(), new Game(2), diceCupESI,
      fieldESI, gameESI, fileIOESI)
    val controller2 = Controller(new Field(2), new DiceCup(), new Game(2), diceCupESI,
      fieldESI, gameESI, fileIOESI)

    "notify its observers on change" in {
      class TestObserver(controller: Controller) extends Observer:
        controller.add(this)
        var bing = false

        def update(e: Event): Unit = {
          bing = true
        }

      when(diceCupESI.sendDiceRequest(any[String], any[String])).thenReturn(None)

      val testObserver = TestObserver(controller)
      testObserver.bing should be(false)
      controller.doAndPublish(controller.dice())
      testObserver.bing should be(true)
    }

    "dices are put out the Dice Cup or in" should {
      when(diceCupESI.sendRequest(any[String], any[String])).thenReturn(DiceCup(List(2, 2), List(3, 3, 3), 2))
      val sortOut: DiceCup = controller.putOut(controller.diceCup.inCup.take(2))
      "be inserted into the locked list of a new DiceCup Object" in {
        sortOut.locked.size should be(2)
        sortOut.inCup.size should be(3)
      }
      "be inserted into the inCup list of a new DiceCup Object" in {
        when(diceCupESI.sendRequest(any[String], any[String])).thenReturn(DiceCup(List(), List(2, 2, 3, 3, 3), 2))
        val putIn: DiceCup = controller.putIn(controller.diceCup.locked.take(2))
        putIn.locked.size should be(0)
        putIn.inCup.size should be(5)
      }
    }

    "dices are thrown" should {
      "contain two lists with all dices" in {
        val thrownDiceCup: DiceCup = controller.dice()
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
        val expectedGame: Game = Game(List(Player(0, "Player 1"), Player(1, "Player 2")), Player(1, "Player 2"), 25,
          List(List(0, 0, 0, 0, 0, 0), List(0, 0, 0, 0, 0, 0)))
        when(gameESI.sendNextRequest(any[Game])).thenReturn(Some(expectedGame))
        controller2.next()
        controller2.game should be(expectedGame)
      }
    }
    "after a Move" when {
      "write down the result" in {
        when(diceCupESI.sendRequest(any[String], any[String])).thenReturn(DiceCup(List(), List(), 2))
        val con = controller.nextRound()
        con.remainingDices should be(2)
        con.inCup.size should be(0)
        con.locked.size should be(0)
      }
    }
    "when toString is called" should {
      "toString" in {
        when(fieldESI.sendStringRequest(any[String], any[String])).thenReturn(controller.field.toString)
        when(diceCupESI.sendStringRequest(any[String], any[String])).thenReturn(controller.diceCup.toString)
        when(gameESI.sendPlayerNameRequest(any[Game])).thenReturn(controller.game.playerName)
        controller.toString should be(controller.field.toString + "\n" + controller.diceCup.toString + "\n"
          + controller.game.playerName + " ist an der Reihe.")
      }
    }
    "when undo/redo/put/save/load is called" should {
      "put" in {
        val expectedGame: Game = Game(List(Player(0, "Player 1"), Player(1, "Player 2")), Player(1, "Player 2"), 25,
          List(List(11, 0, 11, 11, 0, 11), List(0, 0, 0, 0, 0, 0)))

        val expectedField: Field = Field(new Matrix[Option[Int]](2).fill(0, 0, Some(11)))
        when(diceCupESI.sendRequest(any[String], any[String])).thenReturn(new DiceCup())
        when(gameESI.sendStringRequest(any[String], any[String])).thenReturn("")
        when(gameESI.sendRequest(any[String], any[String])).thenReturn(expectedGame)
        when(fieldESI.sendRequest(any[String], any[String])).thenReturn(expectedField)
        controller.put(Move(11, 0, 0))
        controller.field.matrix.cell(0, 0).get should be(11)
      }

      "undo" in {
        when(diceCupESI.sendRequest(any[String], any[String])).thenReturn(new DiceCup())
        when(fieldESI.sendRequest(any[String], any[String])).thenReturn(new Field(2))
        controller.undo()
        controller.field.matrix.cell(0, 0) should be(None)
      }
      "redo" in {
        when(diceCupESI.sendRequest(any[String], any[String])).thenReturn(new DiceCup())
        when(fieldESI.sendRequest(any[String], any[String])).thenReturn(Field(new Matrix[Option[Int]](2).fill(0, 0, Some(11))))
        when(gameESI.sendRequest(any[String], any[String])).thenReturn(new Game(2))
        controller.redo()

        controller.field.matrix.cell(0, 0).get should be(11)
      }
      "save and loaded" in {
        controller.diceCup = DiceCup(List(), List(1, 2, 3, 4, 5), 2)
        doNothing().when(fileIOESI).saveRequest(any[String], any[String])
        when(fileIOESI.loadGameRequest).thenReturn(new Game(2))
        when(fileIOESI.loadFieldRequest).thenReturn(new Field(2))
        when(fileIOESI.loadDiceCupRequest).thenReturn(controller.diceCup)

        controller.save()
        controller.load()
        controller.diceCup should be(DiceCup(List(), List(1, 2, 3, 4, 5), 2))
      }
    }
    "when game quit" should {
      "end the game" in {
        val controller3: Controller = controller
        controller.quit() should be(controller3.quit())
      }
    }
    "when no remaining moves are left game.next" should {
      "be None" in {
        val game: Game = Game(List(Player(1, "Harald"), Player(2, "Dieter")), Player(1, "Harald"), 0, List())
        controller.game = game
        when(gameESI.sendNextRequest(game)).thenReturn(None)
        controller.next()
        controller.game should be(game)
      }
    }
  }
}

