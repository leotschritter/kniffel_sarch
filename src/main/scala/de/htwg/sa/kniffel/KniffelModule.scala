package de.htwg.sa.kniffel

import com.google.inject.AbstractModule
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.controller.controllerBaseImpl.Controller
import net.codingwell.scalaguice.ScalaModule

class KniffelModule extends AbstractModule with ScalaModule {

  val numberOfPlayers: Int = 2

  override def configure(): Unit = {
    bind[IController].toInstance(new Controller(2))
  }

}