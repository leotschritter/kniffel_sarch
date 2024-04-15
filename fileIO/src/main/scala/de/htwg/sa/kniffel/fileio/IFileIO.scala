package de.htwg.sa.kniffel.fileio

import de.htwg.sa.kniffel.field.{IField, IMatrix}
import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.game.IGame

trait IFileIO {
  def loadField: IField

  def loadGame: IGame

  def loadDiceCup: IDiceCup

  def saveField(field: IField, matrix: IMatrix): Unit

  def saveGame(game: IGame): Unit

  def saveDiceCup(diceCup: IDiceCup): Unit

}
