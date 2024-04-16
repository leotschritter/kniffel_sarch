package de.htwg.sa
package controller.controllerBaseImpl

import controller.IController
import model.Move
import de.htwg.sa.util.{Event, Observable, UndoManager}
import com.google.inject.Inject
import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.field.IField
import de.htwg.sa.kniffel.field.fieldBaseImpl.{Field, Matrix}
import de.htwg.sa.kniffel.fileio.IFileIO
import de.htwg.sa.kniffel.fileio.fileIOJsonImpl.FileIO
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.game.gameBaseImpl.Game

class Controller @Inject()(var field: IField, var diceCup: IDiceCup, var game: IGame, var file: IFileIO) extends IController :
  def this() = {
    this(Field(new Matrix[Option[Int]](2)), new DiceCup(), new Game(2), new FileIO())
  }

  val undoManager = new UndoManager[IGame, IField]

  def undo(): Unit = {
    diceCup = diceCup.nextRound()
    val r = undoManager.undoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
  }

  def redo(): Unit = {
    diceCup = diceCup.nextRound()
    val r = undoManager.redoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
  }

  def put(move: Move): Unit = {
    diceCup = diceCup.nextRound()
    val r = undoManager.doStep(game, field, SetCommand(move))
    game = r._1
    field = r._2
  }

  def quit(): Unit = notifyObservers(Event.Quit)

  def next(): Unit =
    game.next().match {
      case Some(g) => game = g
      case None =>
    }

  // doAndPublish for putOut and putIn
  def doAndPublish(doThis: List[Int] => IDiceCup, list: List[Int]): Unit =
    diceCup = doThis(list)
    notifyObservers(Event.Move)

  def putOut(list: List[Int]): IDiceCup =
    diceCup.putDicesOut(list)

  def putIn(list: List[Int]): IDiceCup =
    diceCup.putDicesIn(list)

  // doAndPublish for nextRound() and dice()
  def doAndPublish(doThis: => IDiceCup): Unit =
    diceCup = doThis
    notifyObservers(Event.Move)

  def dice(): IDiceCup = diceCup.dice().getOrElse(diceCup)

  def nextRound(): IDiceCup = diceCup.nextRound()
  
  def save(): Unit = {
    file.saveGame(game)
    file.saveField(field, field.matrix)
    file.saveDiceCup(diceCup)
    notifyObservers(Event.Save)
  }

  def load(): Unit = {
    field = file.loadField
    game = file.loadGame
    diceCup = file.loadDiceCup
    notifyObservers(Event.Load)
  }

  override def toString: String = field.toString