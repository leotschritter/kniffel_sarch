package de.htwg.sa.kniffel.controller.controllerBaseImpl

import de.htwg.sa.kniffel.util.HttpUtil.sendRequest
import de.htwg.sa.kniffel.util.{Event, HttpUtil, Move, Observer}
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsArray, JsDefined, JsNull, JsNumber, Json}


class ControllerSpec extends AnyWordSpec {
  "The Controller" should {
    val controller = new Controller()
    val controller2 = new Controller()

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
      class TestObserver(controller: Controller) extends Observer:
        controller.add(this)
        var bing = false

        def update(e: Event): Unit = {
          bing = true
        }
      val testObserver = TestObserver(controller)
      testObserver.bing should be(false)

      controller2.diceCup = controller2.dice()
      val inCup2: List[Int] = (Json.parse(controller2.diceCup) \ "dicecup" \ "incup").as[List[Int]]
      val resultPutOut = controller2.doAndPublish(controller2.putOut(inCup2.take(2)))
      val sortOut2 = Json.parse(controller2.diceCup) \ "dicecup"
      "be inserted into the locked list of a new DiceCup Object" in {
        (sortOut2 \ "stored").as[List[Int]].size should be (2)
        (sortOut2 \ "incup").as[List[Int]].size should be (3)
        resultPutOut should startWith("    |P1 |P2 \n" +
          "+---+---+---+\n" +
          "|1  |   |   |\n" +
          "+---+---+---+\n" +
          "|2  |   |   |\n" +
          "+---+---+---+\n" +
          "|3  |   |   |\n" +
          "+---+---+---+\n" +
          "|4  |   |   |\n" +
          "+---+---+---+\n" +
          "|5  |   |   |\n" +
          "+---+---+---+\n" +
          "|6  |   |   |\n" +
          "+---+---+---+\n" +
          "|G  |   |   |\n" +
          "+---+---+---+\n" +
          "|B  |   |   |\n" +
          "+---+---+---+\n" +
          "|O  |   |   |\n" +
          "+---+---+---+\n" +
          "|3x |   |   |\n" +
          "+---+---+---+\n" +
          "|4x |   |   |\n" +
          "+---+---+---+\n" +
          "|FH |   |   |\n" +
          "+---+---+---+\n" +
          "|KS |   |   |\n" +
          "+---+---+---+\n" +
          "|GS |   |   |\n" +
          "+---+---+---+\n" +
          "|KN |   |   |\n" +
          "+---+---+---+\n" +
          "|CH |   |   |\n" +
          "+---+---+---+\n" +
          "|U  |   |   |\n" +
          "+---+---+---+\n" +
          "|O  |   |   |\n" +
          "+---+---+---+\n" +
          "|E  |   |   |\n" +
          "+---+---+---+\n")
        testObserver.bing should be(true)
      }
      "be inserted into the inCup list of a new DiceCup Object" in {
        val locked2: List[Int] = (Json.parse(controller2.diceCup) \ "dicecup" \ "stored").as[List[Int]]
        val resultPutIn = controller2.doAndPublish(controller2.putIn(locked2.take(2)))
        (Json.parse(controller2.diceCup) \ "dicecup" \ "stored").as[List[Int]].size should be (0)
        (Json.parse(controller2.diceCup) \ "dicecup" \ "incup").as[List[Int]].size should be (5)
        resultPutIn should startWith("    |P1 |P2 \n" +
          "+---+---+---+\n" +
          "|1  |   |   |\n" +
          "+---+---+---+\n" +
          "|2  |   |   |\n" +
          "+---+---+---+\n" +
          "|3  |   |   |\n" +
          "+---+---+---+\n" +
          "|4  |   |   |\n" +
          "+---+---+---+\n" +
          "|5  |   |   |\n" +
          "+---+---+---+\n" +
          "|6  |   |   |\n" +
          "+---+---+---+\n" +
          "|G  |   |   |\n" +
          "+---+---+---+\n" +
          "|B  |   |   |\n" +
          "+---+---+---+\n" +
          "|O  |   |   |\n" +
          "+---+---+---+\n" +
          "|3x |   |   |\n" +
          "+---+---+---+\n" +
          "|4x |   |   |\n" +
          "+---+---+---+\n" +
          "|FH |   |   |\n" +
          "+---+---+---+\n" +
          "|KS |   |   |\n" +
          "+---+---+---+\n" +
          "|GS |   |   |\n" +
          "+---+---+---+\n" +
          "|KN |   |   |\n" +
          "+---+---+---+\n" +
          "|CH |   |   |\n" +
          "+---+---+---+\n" +
          "|U  |   |   |\n" +
          "+---+---+---+\n" +
          "|O  |   |   |\n" +
          "+---+---+---+\n" +
          "|E  |   |   |\n" +
          "+---+---+---+\n")
        testObserver.bing should be(true)
      }
    }

    "dices are thrown" should {
      "contain two lists with all dices" in {
        val thrownDiceCup = Json.parse(controller.dice()) \ "dicecup"
        (thrownDiceCup \ "incup").as[List[Int]].size + (thrownDiceCup \ "stored").as[List[Int]].size should be(5)
        (thrownDiceCup \ "incup").as[List[Int]].foreach {
          s =>
            s should be < 7
            s should be > 0
        }
        (thrownDiceCup \ "stored").as[List[Int]].foreach {
          s =>
            s should be < 7
            s should be > 0
        }
      }
    }

    "set a new Game object" when {
      "finishing a move" in {
        controller2.next()
        controller2.game should be ("{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":25,\"currentPlayerID\":1,\"currentPlayerName\":\"Player 2\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}")
      }
    }
    "after a Move" when {
      "write down the result" in {
        val con = Json.parse(controller.nextRound()) \ "dicecup"
        (con \ "remainingDices").as[Int] should be(2)
        (con \ "incup").as[List[Int]].size should be(0)
        (con \ "stored").as[List[Int]].size should be(0)
      }
    }
    "when toString is called" should {
      "toString" in {
        val controller3 = new Controller()
        controller3.toString should be(
          "    |P1 |P2 \n" +
          "+---+---+---+\n" +
          "|1  |   |   |\n" +
          "+---+---+---+\n" +
          "|2  |   |   |\n" +
          "+---+---+---+\n" +
          "|3  |   |   |\n" +
          "+---+---+---+\n" +
          "|4  |   |   |\n" +
          "+---+---+---+\n" +
          "|5  |   |   |\n" +
          "+---+---+---+\n" +
          "|6  |   |   |\n" +
          "+---+---+---+\n" +
          "|G  |   |   |\n" +
          "+---+---+---+\n" +
          "|B  |   |   |\n" +
          "+---+---+---+\n" +
          "|O  |   |   |\n" +
          "+---+---+---+\n" +
          "|3x |   |   |\n" +
          "+---+---+---+\n" +
          "|4x |   |   |\n" +
          "+---+---+---+\n" +
          "|FH |   |   |\n" +
          "+---+---+---+\n" +
          "|KS |   |   |\n" +
          "+---+---+---+\n" +
          "|GS |   |   |\n" +
          "+---+---+---+\n" +
          "|KN |   |   |\n" +
          "+---+---+---+\n" +
          "|CH |   |   |\n" +
          "+---+---+---+\n" +
          "|U  |   |   |\n" +
          "+---+---+---+\n" +
          "|O  |   |   |\n" +
          "+---+---+---+\n" +
          "|E  |   |   |\n" +
          "+---+---+---+\n" +
          "Im Becher: \n"+
          "Rausgenommen: \n"+
          "Verbleibende Würfe: 3\n"+
          "Bitte wählen Sie aus: 1 2 3 4 5 6 3X 4X FH KS GS KN CH\n"+
          "Player 1 ist an der Reihe.")
      }
    }
    "when undo/redo/put/save/load is called" should {
      "put" in {
        controller.put(Move(11, 0, 0))
        (Json.parse(sendRequest("field/cell/0/0", controller.field)) \ "value").as[Int] should be (11)
      }
      "undo" in {
        controller.undo()
        (Json.parse(sendRequest("field/cell/0/0", controller.field)) \ "value") should be (JsDefined(JsNull))
      }
      "redo" in {
        controller.redo()
        (Json.parse(sendRequest("field/cell/0/0", controller.field)) \ "value") should be (JsDefined(JsNumber(11)))
      }
      "save and loaded" in {
        controller.diceCup = "{\"dicecup\":{\"stored\":[],\"incup\":[1,2,3,4,5],\"remainingDices\":1}}"
        controller.save()
        controller.load()
        controller.diceCup should be ("{\"dicecup\":{\"stored\":[],\"incup\":[1,2,3,4,5],\"remainingDices\":1}}")
      }
    }

    "when game quit" should {
      "end the game" in {
        val controller4: Controller = controller
        controller.quit() should be(controller4.quit())
      }
    }
    "when no remaining moves are left game.next" should {
      "be None" in {
        val game = "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":0,\"currentPlayerID\":0,\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}"
        controller.game = game
        controller.next()
        controller.game should be(game)
      }
    }

    "when writeDown is called" should {
      "return the changed controller" in {
        controller.writeDown(Move(50, 0, 14))
        val jsArray = (controller.toJson \ "controller" \ "field" \ "rows").as[JsArray]
        jsArray(14)(0) should be (JsNumber(50))
      }
    }

    "when a JSON String is converted to a move" should {
      "have the expected returned move" in {
        controller.jsonStringToMove("{\"move\":{\"value\":12,\"x\":1,\"y\":0}}") should be (Move(12, 1, 0))
      }
    }

    "when parsed from JSON String" should {
      "be the exact same controller again" in {
        controller.jsonStringToController("{\"controller\":{\"dicecup\":{\"stored\":[],\"incup\":[],\"remainingDices\":2},\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]},\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0,\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}}").toJson.toString should be (Controller().toJson.toString)
      }
    }

    "when dice are rolled" should {
      "return a valid diceCup" in {
        controller.diceCup = controller.dice()
        Json.parse(controller.diceCup) \ "dicecup" should not be (JsNull)
      }
      "return a the same diceCup as before" in {
        controller.diceCup = controller.dice()
        controller.diceCup = controller.dice()
        controller.diceCup = controller.dice()
        val diceCupBefore = controller.diceCup
        controller.diceCup = controller.dice()
        diceCupBefore should be (controller.diceCup)
      }
    }

    // Working but not improving coverage
    /*"routes must be reachable" in {
      sendRequest("controller/") should be ("    |P1 |P2 \n+---+---+---+\n|1  |   |   |\n+---+---+---+\n|2  |   |   |\n+---+---+---+\n|3  |   |   |\n+---+---+---+\n|4  |   |   |\n+---+---+---+\n|5  |   |   |\n+---+---+---+\n|6  |   |   |\n+---+---+---+\n|G  |   |   |\n+---+---+---+\n|B  |   |   |\n+---+---+---+\n|O  |   |   |\n+---+---+---+\n|3x |   |   |\n+---+---+---+\n|4x |   |   |\n+---+---+---+\n|FH |   |   |\n+---+---+---+\n|KS |   |   |\n+---+---+---+\n|GS |   |   |\n+---+---+---+\n|KN |   |   |\n+---+---+---+\n|CH |   |   |\n+---+---+---+\n|U  |   |   |\n+---+---+---+\n|O  |   |   |\n+---+---+---+\n|E  |   |   |\n+---+---+---+\nIm Becher: \n"+ "Rausgenommen: \n"+ "Verbleibende Würfe: 3\n"+ "Bitte wählen Sie aus: 1 2 3 4 5 6 3X 4X FH KS GS KN CH\n"+ "Player 1 ist an der Reihe.")
      sendRequest("controller/controller") should be ("{\"controller\":{\"dicecup\":{\"stored\":[],\"incup\":[],\"remainingDices\":2},\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]},\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0,\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}}")
      sendRequest("controller/diceCup") should be ("{\"dicecup\":{\"stored\":[],\"incup\":[],\"remainingDices\":2}}")
      sendRequest("controller/field") should be ("{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}")
      sendRequest("controller/game") should be ("{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0,\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}")
    }*/
  }
}

