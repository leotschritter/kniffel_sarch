package de.htwg.sa.kniffel.controller.model

import de.htwg.sa.kniffel.controller.entity.{DiceCup, Field, Game}
import de.htwg.sa.kniffel.controller.integration.dicecup.DiceCupESI
import de.htwg.sa.kniffel.controller.integration.field.FieldESI
import de.htwg.sa.kniffel.controller.integration.fileio.FileIOESI
import de.htwg.sa.kniffel.controller.integration.game.GameESI
import de.htwg.sa.kniffel.controller.util.{Move, Observable}
import play.api.libs.json.JsObject

trait IController extends Observable {
  def undo(): String

  def redo(): String

  def put(move: Move): String

  def quit(): String

  def next(): String
  
  def putOut(list: List[Int]): DiceCup

  def putIn(list: List[Int]): DiceCup

  def doAndPublish(doThis: => DiceCup): String

  def dice(): DiceCup

  def nextRound(): DiceCup

  def toString: String

  def field: Field

  def diceCup: DiceCup

  def game: Game

  def save(): String

  def load(): String
  
  def load(id: Int): String

  def loadOptions: String

  def toJson: JsObject

  def gameESI: GameESI

  def diceCupESI: DiceCupESI

  def fieldESI: FieldESI

  def fileIOESI: FileIOESI

  def writeDown(move: Move): String
  
  def jsonStringToMove(move: String): Move
}
