package de.htwg.sa.kniffel.field

import com.google.inject.AbstractModule
import de.htwg.sa.kniffel.field.model.IField
import de.htwg.sa.kniffel.field.model.fieldBaseImpl.Field
import net.codingwell.scalaguice.ScalaModule

class FieldModule extends AbstractModule with ScalaModule {
  val numberOfPlayers: Int = 2

  override def configure(): Unit = bind[IField].toInstance(new Field(numberOfPlayers))

}
