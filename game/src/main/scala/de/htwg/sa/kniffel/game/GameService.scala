package de.htwg.sa.kniffel.game

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.game.api.GameApi
import de.htwg.sa.kniffel.game.model.IGame

object GameService {
  private val injector: Injector = Guice.createInjector(new GameModule)

  def main(args: Array[String]): Unit = {
    val diceCupApi = new GameApi(injector.getInstance(classOf[IGame]))
  }
}
