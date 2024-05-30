package de.htwg.sa.kniffel.persistence.persistence.slickImpl.table

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class InCupDice(tag: Tag) extends Table[(Int, Int, Int)](tag, "in_cup_dice") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def value = column[Int]("value")

  def gameId = column[Int]("game_id")

  // default projection
  def * = (id, value, gameId)

  // foreign key
  def games = foreignKey("game_fk", gameId, TableQuery[Games])(_.id)
}
