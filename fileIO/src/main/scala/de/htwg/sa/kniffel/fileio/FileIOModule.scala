package de.htwg.sa.kniffel.fileio

import com.google.inject.AbstractModule
import de.htwg.sa.kniffel.fileio.model.IFileIO
import de.htwg.sa.kniffel.fileio.model.fileIOJsonImpl.FileIO
import net.codingwell.scalaguice.ScalaModule

class FileIOModule extends AbstractModule with ScalaModule {
  override def configure(): Unit =
    bind[IFileIO].toInstance(new FileIO())

}
