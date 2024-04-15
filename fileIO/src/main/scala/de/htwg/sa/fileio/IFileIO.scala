package de.htwg.sa.fileio

import de.htwg.sa.dicecup.IDiceCup
import de.htwg.sa.field.{IField, IMatrix}
import de.htwg.sa.game.IGame

trait IFileIO {
  def loadField: IField

  def loadGame: IGame

  def loadDiceCup: IDiceCup

  def saveField(field: IField, matrix: IMatrix): Unit

  def saveGame(game: IGame): Unit

  def saveDiceCup(diceCup: IDiceCup): Unit

}
