package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import com.google.inject.Inject
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.fileio.IFileIO
import de.htwg.sa.kniffel.fileio.fileIOJsonImpl.FileIO
import de.htwg.sa.kniffel.util.HttpUtil.sendRequest
import de.htwg.sa.kniffel.util.{Event, Move, Observable, UndoManager}
import play.api.libs.json
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}

class Controller @Inject()(var field: String, var diceCup: String, var game: String, var file: IFileIO) extends IController :
  def this() = {
    this("{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}",
      "{\"dicecup\":{\"stored\":[],\"incup\":[],\"remainingDices\":2}}",
      "{\"game\":{\"nestedList\":\"0,0,0,0,0,0;0,0,0,0,0,0\",\"remainingMoves\":26,\"currentPlayerID\":0,\"currentPlayerName\":\"Player 1\",\"players\":[{\"id\":0,\"name\":\"Player 1\"},{\"id\":1,\"name\":\"Player 2\"}]}}",
      new FileIO())
  }

  val undoManager = new UndoManager[String, String]

  def undo(): IController = {
    diceCup = nextRound()
    val r = undoManager.undoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    this
  }

  def redo(): IController = {
    diceCup = nextRound()
    val r = undoManager.redoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    this
  }

  def put(move: Move): IController = {
    diceCup = nextRound()
    val r = undoManager.doStep(game, field, SetCommand(move))
    game = r._1
    field = r._2
    this
  }

  def quit(): IController = {
    notifyObservers(Event.Quit)
    this
  }

  def next(): IController =
    game = getNextGame
    this

  // doAndPublish for putOut and putIn
  def doAndPublish(doThis: List[Int] => String, list: List[Int]): IController =
    diceCup = doThis(list)
    notifyObservers(Event.Move)
    this

  def putOut(list: List[Int]): String =
    sendRequest(s"diceCup/putOut/list=${list.mkString(",")}", diceCup)

  def putIn(list: List[Int]): String =
    sendRequest(s"diceCup/putIn/list=${list.mkString(",")}", diceCup)

  // doAndPublish for nextRound() and dice()
  def doAndPublish(doThis: => String): IController =
    diceCup = doThis
    notifyObservers(Event.Move)
    this

  def dice(): String = {
    val dc = sendRequest("diceCup/dice", diceCup)
    (Json.parse(dc) \ "dicecup").as[JsValue].match {
      case JsNull => diceCup
      case _ => dc
    }
  }

  def nextRound(): String = sendRequest("diceCup/nextRound", diceCup)
  
  def save(): IController = {
    // TODO
    // file.saveGame(game)
    // file.saveField(field, field.matrix)
    // file.saveDiceCup(diceCup)
    notifyObservers(Event.Save)
    this
  }

  def load(): IController = {
    // TODO
    // field = file.loadField
    // game = file.loadGame
    // diceCup = file.loadDiceCup
    notifyObservers(Event.Load)
    this
  }

  private val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
    val ints = str.split("=").tail.mkString(",").split(",").map(_.toInt)
    Some(ints.toList)
  }


  override def toString: String = field

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
            complete(toJson.toString)
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
          path("") {
            sys.error("No such GET route")
          }
        )
      },
      post {
        concat(
          path("undo") {
            complete(undo().toJson.toString)
          },
          path("redo") {
            complete(redo().toJson.toString)
          },
          path("put" / IntNumber / IntNumber / IntNumber) { (value: Int, x: Int, y: Int) =>
            complete(put(Move(value, x, y)).toJson.toString)
          },
          path("quit") {
            complete(quit().toJson.toString)
          },
          path("next") {
            complete(next().toJson.toString)
          },
          // example: putOut/list=1,2,3
          path("putOut" / IntList) { (list: List[Int]) =>
            complete(putOut(list))
          },
          path("putIn" / IntList) { (list: List[Int]) =>
            complete(putIn(list))
          },
          path("dice") {
            complete(dice())
          },
          path("nextRound") {
            complete(nextRound())
          },
          path("save") {
            complete(save().toJson.toString)
          },
          path("load") {
            complete(save().toJson.toString)
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
    new Controller(f, dc, g, new FileIO())
  }