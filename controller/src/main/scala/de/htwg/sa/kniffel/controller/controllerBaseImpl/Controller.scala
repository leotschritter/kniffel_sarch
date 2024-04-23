package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import com.google.inject.Inject
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.util.HttpUtil.sendRequest
import de.htwg.sa.kniffel.util.{Event, Move, Observable, UndoManager}
import play.api.libs.json
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}

class Controller @Inject()(var field: String, var diceCup: String, var game: String) extends IController :
  def this() = {
    this("{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}",
      "{\"dicecup\":{\"stored\":[],\"incup\":[],\"remainingDices\":2}}",
      "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0,\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}")
  }

  val undoManager = new UndoManager[String, String]

  def undo(): String = {
    diceCup = nextRound()
    val r = undoManager.undoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    toString
  }

  def redo(): String = {
    diceCup = nextRound()
    val r = undoManager.redoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    toString
  }

  def put(move: Move): String = {
    diceCup = nextRound()
    val r = undoManager.doStep(game, field, SetCommand(move))
    game = r._1
    field = r._2
    toString
  }

  def quit(): String = {
    notifyObservers(Event.Quit)
    toString
  }

  def next(): String =
    game = getNextGame
    toString

  // doAndPublish for putOut and putIn
  def doAndPublish(doThis: List[Int] => String, list: List[Int]): String =
    diceCup = doThis(list)
    notifyObservers(Event.Move)
    toString

  def putOut(list: List[Int]): String =
    sendRequest(s"diceCup/putOut/list=${list.mkString(",")}", diceCup)

  def putIn(list: List[Int]): String =
    sendRequest(s"diceCup/putIn/list=${list.mkString(",")}", diceCup)

  // doAndPublish for nextRound() and dice()
  def doAndPublish(doThis: => String): String =
    diceCup = doThis
    notifyObservers(Event.Move)
    toString

  def dice(): String = {
    val dc = sendRequest("diceCup/dice", diceCup)
    (Json.parse(dc) \ "dicecup").as[JsValue].match {
      case JsNull => diceCup
      case _ => dc
    }
  }

  def nextRound(): String = sendRequest("diceCup/nextRound", diceCup)

  def save(): String = {
    sendRequest("io/saveField", field)
    sendRequest("io/saveGame", game)
    sendRequest("io/saveDiceCup", diceCup)
    notifyObservers(Event.Save)
    toString
  }

  def load(): String = {
    field = sendRequest("io/loadField")
    game = sendRequest("io/loadGame")
    diceCup = sendRequest("io/loadDiceCup")
    notifyObservers(Event.Load)
    toString
  }

  private val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
    val ints = str.split("=").tail.mkString(",").split(",").map(_.toInt)
    Some(ints.toList)
  }

  private val StringValue: PathMatcher1[String] = PathMatcher("""\w+""".r)

  override def toString: String =
    s"${sendRequest("field/mesh", field)}\n${sendRequest("diceCup/representation", diceCup)}\n$getPlayerName ist an der Reihe."

  override def toJson: JsObject = {
    Json.obj(
      "controller" ->
        Json.parse(this.diceCup).as[JsObject]
          .deepMerge(Json.parse(this.field).as[JsObject])
          .deepMerge(Json.parse(this.game).as[JsObject]))
  }

  override val controllerRoute: Route =
    concat(
      get {
        concat(
          pathSingleSlash {
            complete(toString)
          },
          path("field") {
            complete(field)
          },
          path("game") {
            complete(game)
          },
          path("diceCup") {
            complete(diceCup)
          },
          path("load") {
            complete(load())
          },
          path("next") {
            complete(next())
          },
          pathPrefix("doAndPublish") {
            concat(
              path("nextRound") {
                complete(doAndPublish(nextRound()))
              },
              path("dice") {
                complete(doAndPublish(dice()))
              },
              path("putIn" / IntList) {
                (pi: List[Int]) =>
                  complete(doAndPublish(putIn(pi)))
              },
              path("putOut" / IntList) {
                (po: List[Int]) =>
                  complete(doAndPublish(putOut(po)))
              }
            )
          },
          path("save") {
            complete(save())
          },
          path("undo") {
            complete(undo())
          },
          path("redo") {
            complete(redo())
          },
          path("writeDown" / StringValue) {
            (value: String) =>
              try {
                val currentPlayer = (Json.parse(sendRequest("game/playerID", game)) \ "playerID").as[Int]
                val indexOfField = (Json.parse(sendRequest("diceCup/indexOfField")) \ "indexOfField" \ value).as[Int]
                val result = (Json.parse(sendRequest(s"diceCup/result/$indexOfField", diceCup)) \ "result").as[Int]
                complete(writeDown(Move(result, currentPlayer, indexOfField)))
              } catch {
                case e: Throwable => complete("Invalid Input!")
              }
          },
          path("") {
            sys.error("No such GET route")
          }
        )
      },
      post {
        concat(
          path("put") {
            entity(as[String]) { requestBody =>
              complete(this.put(jsonStringToMove(requestBody)))
            }
          },
          path("quit") {
            complete(quit())
          },
          path("nextRound") {
            complete(nextRound())
          },
          path("") {
            sys.error("No such POST route")
          }
        )
      }
    )

  private def getNextGame: String = {
    val nextGameString = sendRequest("game/next", this.game)
    val nextGameJson = Json.parse(nextGameString)
    (nextGameJson \ "game").as[JsValue].match {
      case JsNull => JsNull.toString
      case _ => nextGameString
    }
  }

  private def jsonStringToController(controller: String): IController = {
    val controllerJson = Json.parse(controller)
    val f = Json.obj("field" -> (controllerJson \ "controller" \ "field").as[JsObject]).toString
    val dc = Json.obj("dicecup" -> (controllerJson \ "controller" \ "dicecup").as[JsObject]).toString
    val g = Json.obj("game" -> (controllerJson \ "controller" \ "game").as[JsObject]).toString
    new Controller(f, dc, g)
  }

  private def jsonStringToMove(move: String): Move = {
    Move(
      (Json.parse(move) \ "move" \ "value").as[Int],
      (Json.parse(move) \ "move" \ "x").as[Int],
      (Json.parse(move) \ "move" \ "y").as[Int]
    )
  }

  private def getPlayerName: String = (Json.parse(sendRequest("game/playerName", game)) \ "playerName").as[String]

  private def writeDown(move: Move): String = {
    put(move)
    next()
    doAndPublish(nextRound())
  }