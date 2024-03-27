package de.htwg.se.kniffel
package model.fileIOComponent

import model.dicecupComponent.IDiceCup
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
