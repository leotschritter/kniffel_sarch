package de.htwg.sa.kniffel.fileio

import de.htwg.sa.kniffel.fileio.api.FileIoApi
import de.htwg.sa.kniffel.fileio.model.IFileIO
import de.htwg.sa.kniffel.fileio.model.fileIOXmlImpl.FileIO

object FileIOService:
  val fileIO: IFileIO = new FileIO()
  given IFileIO = fileIO 
  
  def main(args: Array[String]): Unit = new FileIoApi()