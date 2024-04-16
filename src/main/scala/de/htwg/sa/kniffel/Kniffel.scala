package de.htwg.sa.kniffel

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.gui.GUI
import de.htwg.sa.kniffel.tui.TUI

object Kniffel {

  private val injector: Injector = Guice.createInjector(new KniffelModule)
  val controller: IController = injector.getInstance(classOf[IController])
  private val tui = injector.getInstance(classOf[TUI])

  def main(args: Array[String]): Unit = {
    injector.getInstance(classOf[GUI])
    println("Welcome to Kniffel")
    tui.run()
  }
}