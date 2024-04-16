package de.htwg.sa.kniffel

import controller.IController
import controller.controllerBaseImpl.Controller
import de.htwg.sa.kniffel.field.IField
import de.htwg.sa.kniffel.fileio.fileIOXmlImpl.FileIO
import de.htwg.sa.kniffel.field.fieldBaseImpl.{Field, Matrix}
import de.htwg.sa.kniffel.fileio.IFileIO
import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.game.gameBaseImpl.Game

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
