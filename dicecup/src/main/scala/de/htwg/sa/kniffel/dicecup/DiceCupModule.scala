package de.htwg.sa.kniffel.dicecup

import com.google.inject.AbstractModule
import de.htwg.sa.kniffel.dicecup.model.IDiceCup
import de.htwg.sa.kniffel.dicecup.model.dicecupBaseImpl.DiceCup
import net.codingwell.scalaguice.ScalaModule

class DiceCupModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = bind[IDiceCup].toInstance(new DiceCup())

}
