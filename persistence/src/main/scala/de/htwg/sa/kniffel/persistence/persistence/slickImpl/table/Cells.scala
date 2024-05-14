package de.htwg.sa.kniffel.persistence.persistence.slickImpl.table

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class Cells(tag: Tag) extends Table[(Int, Int, Int, Option[Int], Int)](tag, "cell") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def x = column[Int]("x")

  def y = column[Int]("y")

  def value = column[Option[Int]]("val")

  def gameId = column[Int]("game_id")

  // default projection
  def * = (id, x, y, value, gameId)

  // foreign key
  def games = foreignKey("game_fk", gameId, TableQuery[Games])(_.id)
}
