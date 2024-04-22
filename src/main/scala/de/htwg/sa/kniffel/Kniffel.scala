package de.htwg.sa.kniffel

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.rest.Rest

object Kniffel {

  private val injector: Injector = Guice.createInjector(new KniffelModule)

  def main(args: Array[String]): Unit = {
    val rest: Rest = Rest(injector.getInstance(classOf[IController]))
    println("Welcome to Kniffel")
  }
}