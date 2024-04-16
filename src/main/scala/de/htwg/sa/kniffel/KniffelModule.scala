package de.htwg.sa.kniffel

import com.google.inject.AbstractModule
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.controller.controllerBaseImpl.Controller
import de.htwg.sa.kniffel.dicecup.IDiceCup
import de.htwg.sa.kniffel.dicecup.dicecupBaseImpl.DiceCup
import de.htwg.sa.kniffel.field.{IField, IMatrix}
import de.htwg.sa.kniffel.field.fieldBaseImpl.{Field, Matrix}
import de.htwg.sa.kniffel.fileio.IFileIO
import de.htwg.sa.kniffel.fileio.fileIOJsonImpl.FileIO
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.game.gameBaseImpl.Game
import net.codingwell.scalaguice.ScalaModule

class KniffelModule extends AbstractModule with ScalaModule {

  val numberOfPlayers: Int = 2

  override def configure(): Unit = {
    bind[IMatrix].toInstance(new Matrix[String](numberOfPlayers))
    bind[IField].toInstance(new Field(numberOfPlayers))
    bind[IGame].toInstance(new Game(numberOfPlayers))
    bind[IDiceCup].toInstance(new DiceCup())
    bind[IFileIO].toInstance(new FileIO())
    bind[IController].toInstance(new Controller())
  }

}