package de.htwg.sa.kniffel.field

import com.google.inject.{Guice, Injector}
import de.htwg.sa.kniffel.field.api.FieldApi
import de.htwg.sa.kniffel.field.model.IField

object FieldService {
  
  val injector: Injector = Guice.createInjector(new FieldModule)

  def main(args: Array[String]): Unit = {
    val fileIoApi: FieldApi = new FieldApi(injector.getInstance(classOf[IField]))
  }
}
