package de.htwg.sa.kniffel.controller

import com.google.inject.AbstractModule
import de.htwg.sa.kniffel.controller.model.IController
import de.htwg.sa.kniffel.controller.model.controllerBaseImpl.Controller
import net.codingwell.scalaguice.ScalaModule

class RestControllerModule extends AbstractModule with ScalaModule:

  val numberOfPlayers: Int = 2

  override def configure(): Unit = 
    bind[IController].toInstance(new Controller(numberOfPlayers))
  