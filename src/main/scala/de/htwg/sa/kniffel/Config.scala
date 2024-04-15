package de.htwg.sa.kniffel

import controller.IController
import controller.controllerBaseImpl.Controller
import de.htwg.sa.dicecup.IDiceCup
import de.htwg.sa.dicecup.dicecupBaseImpl.DiceCup
import model.fileIOComponent.IFileIO
import model.fileIOComponent.fileIOXmlImpl.FileIO
import model.gameComponent.IGame
import model.gameComponent.gameBaseImpl.Game
import model.fieldComponent.{IField, IMatrix}
import model.fieldComponent.fieldBaseImpl.{Field, Matrix}

object Config {
  val numberOfPLayers = 2
  val field = new Field(numberOfPLayers)
  given IField = field

  val dicecup = new DiceCup()
  given IDiceCup = dicecup

  val game = new Game(numberOfPLayers)
  given IGame = game

  val fileIO = new FileIO
  given IFileIO = fileIO

  val controller = new Controller()
  given IController = controller
}
