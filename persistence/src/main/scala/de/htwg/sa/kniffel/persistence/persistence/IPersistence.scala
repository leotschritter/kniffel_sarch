package de.htwg.sa.kniffel.persistence.persistence

trait IPersistence {
  def loadField: String

  def loadGame: String

  def loadDiceCup: String

  def saveField(field: String): String

  def saveGame(game: String): String

  def saveDiceCup(diceCup: String): String

  def createGame(numberOfPlayers: Int): String
}
