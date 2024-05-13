package de.htwg.sa.kniffel.persistence.table

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class Players(tag: Tag) extends Table[(Int, String, Boolean, Int, Int, Int, Int, Int, Int)](tag, "player") {
  private def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  private def name = column[String]("name")

  private def isTurn = column[Boolean]("is_turn", O.Default(false))

  private def top = column[Int]("top")

  private def bonus = column[Int]("bonus")

  private def sumTop = column[Int]("sum_top")

  private def sumBottom = column[Int]("sum_bottom")

  private def total = column[Int]("total")

  private def gameId = column[Int]("game_id")

  // default projection
  def * = (id, name, isTurn, top, bonus, sumTop, sumBottom, total, gameId)

  // foreign key
  def games = foreignKey("game_fk", gameId, TableQuery[Games])(_.id)
}
