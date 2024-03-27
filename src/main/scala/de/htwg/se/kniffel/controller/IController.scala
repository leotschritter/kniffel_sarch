package de.htwg.se.kniffel
package controller

import model.fieldComponent.IField
import model.gameComponent.IGame
import util.Observable
import model.dicecupComponent.IDiceCup
import model.Move

trait IController extends Observable {
  def undo(): Unit

  def redo(): Unit

  def put(move: Move): Unit

  def quit(): Unit

  def next(): Unit

  def doAndPublish(doThis: List[Int] => IDiceCup, list: List[Int]): Unit

  def putOut(list: List[Int]): IDiceCup

  def putIn(list: List[Int]): IDiceCup

  def doAndPublish(doThis: => IDiceCup): Unit

  def dice(): IDiceCup

  def nextRound(): IDiceCup

  def toString: String

  def getField: IField

  def getDicecup: IDiceCup

  def getGame: IGame

  def save: Unit

  def load: Unit
}
