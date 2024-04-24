package de.htwg.sa.kniffel.dicecup

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.dicecup.api.DiceCupApi
import de.htwg.sa.kniffel.dicecup.model.IDiceCup

object DiceCupService {
  private val injector: Injector = Guice.createInjector(new DiceCupModule)

  def main(args: Array[String]): Unit = {
    val diceCupApi = new DiceCupApi(injector.getInstance(classOf[IDiceCup]))
  }
}
