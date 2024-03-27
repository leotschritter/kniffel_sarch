package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import controller.IController
import model.Move
import model.dicecupComponent.IDiceCup
import model.fieldComponent.IField
import model.gameComponent.IGame
import util.{Event, Observable, UndoManager}
import Config.given
import model.fileIOComponent.IFileIO

class Controller(using var field: IField, var diceCup: IDiceCup, var game: IGame, var file: IFileIO) extends IController :

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
    game = game.next().get

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

  def dice(): IDiceCup = diceCup.dice()

  def nextRound(): IDiceCup = diceCup.nextRound()

  def getField: IField = field

  def getDicecup: IDiceCup = diceCup

  def getGame: IGame = game

  def save: Unit = {
    file.saveGame(game)
    file.saveField(field, field.getMatrix)
    file.saveDiceCup(diceCup)
    notifyObservers(Event.Save)
  }

  def load: Unit = {
    field = file.loadField
    game = file.loadGame
    diceCup = file.loadDiceCup
    notifyObservers(Event.Load)
  }

  override def toString: String = field.toString