package de.htwg.sa.kniffel.game

import de.htwg.sa.kniffel.game.api.GameApi
import de.htwg.sa.kniffel.game.model.IGame
import de.htwg.sa.kniffel.game.model.gameBaseImpl.Game

object GameService:
  val numberOfPlayers: Int = 2
  val game: IGame = new Game(numberOfPlayers)
  given IGame = game

  @main def main(): Unit = GameApi().start