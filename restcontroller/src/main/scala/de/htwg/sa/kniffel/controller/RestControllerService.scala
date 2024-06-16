package de.htwg.sa.kniffel.controller

import de.htwg.sa.kniffel.controller.api.{RestControllerApi, RestControllerKafkaApi}
import de.htwg.sa.kniffel.controller.integration.gui.GuiESI
import de.htwg.sa.kniffel.controller.integration.tui.TuiESI
import de.htwg.sa.kniffel.controller.model.IController
import de.htwg.sa.kniffel.controller.model.controllerBaseImpl.Controller

// @formatter:off
object RestControllerService:
  val numberOfPlayers: Int = 2
  val controller: IController = new Controller(numberOfPlayers)
  val guiESI: GuiESI = GuiESI()
  val tuiESI: TuiESI = TuiESI()
  given IController = controller
  given GuiESI = guiESI
  given TuiESI = tuiESI

  @main def main(): Unit =
    RestControllerKafkaApi().start()
    RestControllerApi().start

// @formatter:on
