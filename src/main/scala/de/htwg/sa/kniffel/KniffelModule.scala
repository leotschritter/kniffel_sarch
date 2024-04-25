package de.htwg.sa.kniffel

import com.google.inject.AbstractModule

import net.codingwell.scalaguice.ScalaModule

class KniffelModule extends AbstractModule with ScalaModule {

  val numberOfPlayers: Int = 2

  override def configure(): Unit = ???

}