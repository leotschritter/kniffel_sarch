package de.htwg.sa.kniffel

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.rest.Rest
import de.htwg.sa.kniffel.tui.TUI

object Kniffel {

  private val injector: Injector = Guice.createInjector(new KniffelModule)
  private val tui = injector.getInstance(classOf[TUI])
  val rest = Rest(injector.getInstance(classOf[IController]))

  def main(args: Array[String]): Unit = {
    println("Welcome to Kniffel")
    tui.run()
  }
}