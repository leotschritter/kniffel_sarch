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
  
  def deleteGame(gameId: Int): Unit
  
  def deleteField(gameId: Int): Unit
  
  def deleteInCup(gameId: Int): Unit
  
  def deleteStoredDice(gameId: Int): Unit
  
  def updateGame(game: String, gameId: Int): Unit
  
  def updateField(field: String, gameId: Int): Unit
  
  def updateDiceCup(diceCup: String, gameId: Int): Unit
}
