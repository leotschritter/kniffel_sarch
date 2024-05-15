package de.htwg.sa.kniffel.persistence.persistence

trait IPersistence {
  def loadField: String

  def loadGame: String

  def loadDiceCup: String

  def saveField(field: String): String

  def saveGame(game: String): String

  def saveDiceCup(diceCup: String): String

  def createGame(numberOfPlayers: Int): String

  def loadField(gameId: Int): String

  def loadGame(gameId: Int): String

  def loadDiceCup(gameId: Int): String
  
  def loadOptions: String
}
