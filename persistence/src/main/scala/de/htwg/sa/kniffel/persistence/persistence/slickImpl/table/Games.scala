package de.htwg.sa.kniffel.persistence.persistence.slickImpl.table

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class Games(tag: Tag) extends Table[(Int, Int, Int)](tag, "game") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def remMoves = column[Int]("rem_moves")

  def remDice = column[Int]("rem_dice")

  // default projection
  def * = (id, remMoves, remDice)
}
