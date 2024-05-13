package de.htwg.sa.kniffel.persistence.table

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class Cells(tag: Tag) extends Table[(Int, Int, Int, Int, Int)](tag, "cell") {
  private def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  private def x = column[Int]("x")

  private def y = column[Int]("y")

  private def value = column[Int]("val")

  private def gameId = column[Int]("game_id")

  // default projection
  def * = (id, x, y, value, gameId)

  // foreign key
  def games = foreignKey("game_fk", gameId, TableQuery[Games])(_.id)
}
