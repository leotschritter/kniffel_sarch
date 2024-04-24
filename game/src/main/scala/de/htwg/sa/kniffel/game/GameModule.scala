package de.htwg.sa.kniffel.game

import com.google.inject.AbstractModule
import de.htwg.sa.kniffel.game.model.IGame
import de.htwg.sa.kniffel.game.model.gameBaseImpl.Game
import net.codingwell.scalaguice.ScalaModule

class GameModule extends AbstractModule with ScalaModule {
  val numberOfPlayers: Int = 2

  override def configure(): Unit = {
    bind[IGame].toInstance(new Game(numberOfPlayers))
  }

}
