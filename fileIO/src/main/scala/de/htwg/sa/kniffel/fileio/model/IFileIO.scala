package de.htwg.sa.kniffel.fileio.model

trait IFileIO {
  def loadField: String

  def loadGame: String

  def loadDiceCup: String

  def saveField(field: String): String

  def saveGame(game: String): String

  def saveDiceCup(diceCup: String): String
}
