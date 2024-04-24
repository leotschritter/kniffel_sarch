package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import com.google.inject.Inject
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.field.IField
import de.htwg.sa.kniffel.field.fieldBaseImpl.Field
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.game.gameBaseImpl.Game
import de.htwg.sa.kniffel.util.HttpUtil.sendRequest
import de.htwg.sa.kniffel.util.{Event, Move, Observable, UndoManager}
import play.api.libs.json
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}

import scala.util.Try

class Controller @Inject()(var field: IField, var diceCup: IDiceCup, var game: IGame) extends IController:
  def this(numberOfPlayers: Int) = {
    this(new Field(numberOfPlayers), new DiceCup(), new Game(numberOfPlayers))
  }

  val undoManager = new UndoManager[IGame, IField]

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
  def doAndPublish(doThis: List[Int] => IDiceCup, list: List[Int]): String =
    diceCup = doThis(list)
    notifyObservers(Event.Move)
    toString

  def putOut(list: List[Int]): IDiceCup =
    unpackDiceCup(sendRequest(s"diceCup/putOut/list=${list.mkString(",")}", diceCup.toJson.toString))

  def putIn(list: List[Int]): IDiceCup =
    unpackDiceCup(sendRequest(s"diceCup/putIn/list=${list.mkString(",")}", diceCup.toJson.toString))

  // doAndPublish for nextRound() and dice()
  def doAndPublish(doThis: => IDiceCup): String =
    diceCup = doThis
    notifyObservers(Event.Move)
    toString

  def dice(): IDiceCup = {
    val dc = sendRequest("diceCup/dice", diceCup.toJson.toString)
    (Json.parse(dc) \ "dicecup").as[JsValue].match {
      case JsNull => diceCup
      case _ => unpackDiceCup(dc)
    }
  }

  def nextRound(): IDiceCup =
    unpackDiceCup(sendRequest("diceCup/nextRound", diceCup.toJson.toString))

  def save(): String = {
    sendRequest("io/saveField", field.toJson.toString)
    sendRequest("io/saveGame", game.toJson.toString)
    sendRequest("io/saveDiceCup", diceCup.toJson.toString)
    notifyObservers(Event.Save)
    toString
  }

  def load(): String = {
    field = unpackField(sendRequest("io/loadField"))
    game = unpackGame(sendRequest("io/loadGame"))
    diceCup = unpackDiceCup(sendRequest("io/loadDiceCup"))
    notifyObservers(Event.Load)
    toString
  }

  private val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
    Some(str.split("=").tail.mkString(",").split(",").map(_.toInt).toList)
  }

  private val StringValue: PathMatcher1[String] = PathMatcher("""\w+""".r)

  override def toString: String =
    s"${sendRequest("field/mesh", field.toJson.toString)}\n${sendRequest("diceCup/representation", diceCup.toJson.toString)}\n$getPlayerName ist an der Reihe."

  override def toJson: JsObject = {
    Json.obj(
      "controller" ->
        diceCup.toJson
          .deepMerge(field.toJson)
          .deepMerge(game.toJson))
  }

  override val controllerRoute: Route =
    concat(
      get {
        concat(
          pathSingleSlash {
            complete(toString)
          },
          path("controller") {
            complete(toJson.toString)
          },
          path("field") {
            complete(field.toJson.toString)
          },
          path("game") {
            complete(game.toJson.toString)
          },
          path("diceCup") {
            complete(diceCup.toJson.toString)
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
                val c = doAndPublish(dice())
                print(c)
                complete(c)
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
                val currentPlayer = (Json.parse(sendRequest("game/playerID", game.toJson.toString)) \ "playerID").as[Int]
                val indexOfField = (Json.parse(sendRequest("diceCup/indexOfField")) \ "indexOfField" \ value).as[Int]
                val result = (Json.parse(sendRequest(s"diceCup/result/$indexOfField", diceCup.toJson.toString)) \ "result").as[Int]
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
            complete(nextRound().toJson.toString)
          },
          path("") {
            sys.error("No such POST route")
          }
        )
      }
    )

  private def getNextGame: IGame = {
    Try(unpackGame(sendRequest("game/next", game.toJson.toString))).toOption
      .getOrElse(game)
  }

  def jsonStringToController(controller: String): IController = {
    val controllerJson = Json.parse(controller)
    val f = Json.obj("field" -> (controllerJson \ "controller" \ "field").as[JsObject]).toString
    val dc = Json.obj("dicecup" -> (controllerJson \ "controller" \ "dicecup").as[JsObject]).toString
    val g = Json.obj("game" -> (controllerJson \ "controller" \ "game").as[JsObject]).toString
    new Controller(unpackField(f), unpackDiceCup(dc), unpackGame(g))
  }

  def jsonStringToMove(move: String): Move = {
    Move(
      (Json.parse(move) \ "move" \ "value").as[Int],
      (Json.parse(move) \ "move" \ "x").as[Int],
      (Json.parse(move) \ "move" \ "y").as[Int]
    )
  }

  private def getPlayerName: String = (Json.parse(sendRequest("game/playerName", game.toJson.toString)) \ "playerName").as[String]

  def writeDown(move: Move): String = {
    put(move)
    next()
    doAndPublish(nextRound())
  }

  private def unpackDiceCup(diceCup: String): IDiceCup =
    this.diceCup.jsonStringToDiceCup(diceCup)

  private def unpackGame(game: String): IGame =
    this.game.jsonStringToGame(game)

  private def unpackField(field: String): IField =
    this.field.jsonStringToField(field)