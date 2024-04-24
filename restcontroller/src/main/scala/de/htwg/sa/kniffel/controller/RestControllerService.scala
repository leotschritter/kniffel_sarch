package de.htwg.sa.kniffel.controller

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.controller.api.Rest
import de.htwg.sa.kniffel.controller.model.IController

object RestControllerService {

  private val injector: Injector = Guice.createInjector(new RestControllerModule)

  def main(args: Array[String]): Unit = {
    val rest: Rest = new Rest(injector.getInstance(classOf[IController]))
    println("Welcome to Kniffel")
  }

}
