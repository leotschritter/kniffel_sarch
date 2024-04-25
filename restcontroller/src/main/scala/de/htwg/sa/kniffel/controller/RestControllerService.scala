package de.htwg.sa.kniffel.controller

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.controller.api.RestControllerApi
import de.htwg.sa.kniffel.controller.model.IController

object RestControllerService {

  private val injector: Injector = Guice.createInjector(new RestControllerModule)

  def main(args: Array[String]): Unit = {
    val rest: RestControllerApi = new RestControllerApi(injector.getInstance(classOf[IController]))
    println("Welcome to Kniffel")
  }

}
