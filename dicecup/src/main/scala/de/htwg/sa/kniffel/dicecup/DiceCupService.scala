package de.htwg.sa.kniffel.dicecup

import de.htwg.sa.kniffel.dicecup.api.DiceCupApi
import de.htwg.sa.kniffel.dicecup.model.IDiceCup
import de.htwg.sa.kniffel.dicecup.model.dicecupBaseImpl.DiceCup

object DiceCupService:
  val diceCup: IDiceCup = new DiceCup()
  given IDiceCup = diceCup

  @main def main(): Unit = DiceCupApi().start