package de.htwg.sa.kniffel.fileio

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.fileio.api.FileIoApi
import de.htwg.sa.kniffel.fileio.model.IFileIO

object FileIOService {
  private val injector: Injector = Guice.createInjector(new FileIOModule)

  def main(args: Array[String]): Unit = {
    val fileIoApi: FileIoApi = new FileIoApi(injector.getInstance(classOf[IFileIO]))
  }
}
