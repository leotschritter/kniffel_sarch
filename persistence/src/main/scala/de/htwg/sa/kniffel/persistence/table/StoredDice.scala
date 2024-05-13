package de.htwg.sa.kniffel.persistence.table

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class StoredDice(tag: Tag) extends Table[(Int, Int, Int)](tag, "stored_dice") {
  private def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  private def value = column[Int]("value")

  private def gameId = column[Int]("game_id")

  // default projection
  def * = (id, value, gameId)

  // foreign key
  def games = foreignKey("game_fk", gameId, TableQuery[Games])(_.id)
}
