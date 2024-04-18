package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import controller.IController
import de.htwg.sa.kniffel.util.{Event, Move, Observable, UndoManager}
import com.google.inject.Inject
import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.field.IField
import de.htwg.sa.kniffel.field.fieldBaseImpl.{Field, Matrix}
import de.htwg.sa.kniffel.fileio.IFileIO
import de.htwg.sa.kniffel.fileio.fileIOJsonImpl.FileIO
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.game.gameBaseImpl.Game
import play.api.libs.json.{JsObject, Json}

class Controller @Inject()(var field: IField, var diceCup: IDiceCup, var game: IGame, var file: IFileIO) extends IController :
  def this() = {
    this(Field(new Matrix[Option[Int]](2)), new DiceCup(), new Game(2), new FileIO())
  }

  val undoManager = new UndoManager[IGame, IField]

  def undo(): IController = {
    diceCup = diceCup.nextRound()
    val r = undoManager.undoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    this
  }

  def redo(): IController = {
    diceCup = diceCup.nextRound()
    val r = undoManager.redoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    this
  }

  def put(move: Move): IController = {
    diceCup = diceCup.nextRound()
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
    game.next().match {
      case Some(g) => game = g
      case None =>
    }
    this

  // doAndPublish for putOut and putIn
  def doAndPublish(doThis: List[Int] => IDiceCup, list: List[Int]): IController =
    diceCup = doThis(list)
    notifyObservers(Event.Move)
    this

  def putOut(list: List[Int]): IDiceCup =
    diceCup.putDicesOut(list)

  def putIn(list: List[Int]): IDiceCup =
    diceCup.putDicesIn(list)

  // doAndPublish for nextRound() and dice()
  def doAndPublish(doThis: => IDiceCup): IController =
    diceCup = doThis
    notifyObservers(Event.Move)
    this

  def dice(): IDiceCup = diceCup.dice().getOrElse(diceCup)

  def nextRound(): IDiceCup = diceCup.nextRound()
  
  def save(): IController = {
    file.saveGame(game)
    file.saveField(field, field.matrix)
    file.saveDiceCup(diceCup)
    notifyObservers(Event.Save)
    this
  }

  def load(): IController = {
    field = file.loadField
    game = file.loadGame
    diceCup = file.loadDiceCup
    notifyObservers(Event.Load)
    this
  }

  val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
    val ints = str.split("=").tail.mkString(",").split(",").map(_.toInt)
    Some(ints.toList)
  }


  override def toString: String = field.toString

  override def toJson: JsObject = {
    Json.obj(
      "controller" ->
        this.diceCup.toJson
          .deepMerge(this.field.toJson)
          .deepMerge(this.game.toJson))
  }

  override val controllerRoute: Route =
    concat(
      get {
        concat(
          path("field") {
            complete(field.toJson.toString)
          },
          path("game") {
            complete(game.toJson.toString)
          },
          path("diceCup") {
            complete(diceCup.toJson.toString)
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
            complete(putOut(list).toJson.toString)
          },
          path("putIn" / IntList) { (list: List[Int]) =>
            complete(putIn(list).toJson.toString)
          },
          path("dice") {
            complete(dice().toJson.toString)
          },
          path("nextRound") {
            complete(nextRound().toJson.toString)
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