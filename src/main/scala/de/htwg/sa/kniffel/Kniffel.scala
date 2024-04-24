package de.htwg.sa.kniffel

import com.google.inject.{Guice, Injector}


object Kniffel {

  private val injector: Injector = Guice.createInjector(new KniffelModule)

  def main(args: Array[String]): Unit = {
    println("Welcome to Kniffel")
  }
}