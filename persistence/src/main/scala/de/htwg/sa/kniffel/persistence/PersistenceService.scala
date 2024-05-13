package de.htwg.sa.kniffel.persistence

import table.{Cells, Games, InCupDice, Players, StoredDice}
import slick.jdbc.PostgresProfile.api.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.*

object PersistenceService:
  private val db = Database.forConfig("postgres")

  private val games = TableQuery[Games]
  private val cells = TableQuery[Cells]
  private val inCupDice = TableQuery[InCupDice]
  private val storedDice = TableQuery[StoredDice]
  private val players = TableQuery[Players]

  private val schema = games.schema ++ cells.schema ++ inCupDice.schema ++ storedDice.schema ++ players.schema

  @main def main(): Unit =  {
    db.run(DBIO.seq(
      schema.createIfNotExists
    ))
    Await.result(Future.never, Duration.Inf)
  }