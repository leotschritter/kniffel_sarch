package de.htwg.sa.kniffel.persistence.persistence.slickImpl.table

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class Players(tag: Tag) extends Table[(Int, String, Boolean, Int, Int, Int, Int, Int, Int)](tag, "player") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def isTurn = column[Boolean]("is_turn", O.Default(false))

  def top = column[Int]("top")

  def bonus = column[Int]("bonus")

  def sumTop = column[Int]("sum_top")

  def sumBottom = column[Int]("sum_bottom")

  def total = column[Int]("total")

  def gameId = column[Int]("game_id")

  // default projection
  def * = (id, name, isTurn, top, bonus, sumTop, sumBottom, total, gameId)

  // foreign key
  def games = foreignKey("game_fk", gameId, TableQuery[Games])(_.id)
}
