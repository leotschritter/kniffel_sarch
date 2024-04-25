package de.htwg.sa.kniffel.field

import de.htwg.sa.kniffel.field.api.FieldApi
import de.htwg.sa.kniffel.field.model.IField
import de.htwg.sa.kniffel.field.model.fieldBaseImpl.Field


object FieldService:
  val numberOfPlayers:Int = 2
  val field: IField = new Field(numberOfPlayers)
  given IField = field

  def main(args: Array[String]): Unit = FieldApi()