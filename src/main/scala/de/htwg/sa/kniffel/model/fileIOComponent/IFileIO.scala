package de.htwg.sa.kniffel
package model.fileIOComponent

import de.htwg.sa.dicecup.IDiceCup
import model.fieldComponent.{IField, IMatrix}
import model.gameComponent.IGame

trait IFileIO {
  def loadField: IField

  def loadGame: IGame

  def loadDiceCup: IDiceCup
  
  def saveField(field: IField, matrix: IMatrix): Unit

  def saveGame(game: IGame): Unit

  def saveDiceCup(diceCup: IDiceCup): Unit
  
}
