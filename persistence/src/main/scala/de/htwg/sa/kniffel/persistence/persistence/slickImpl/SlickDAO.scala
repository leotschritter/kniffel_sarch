package de.htwg.sa.kniffel.persistence.persistence.slickImpl

import de.htwg.sa.kniffel.persistence.persistence.IPersistence
import de.htwg.sa.kniffel.persistence.persistence.slickImpl.table.*
import play.api.libs.json.{JsArray, JsBoolean, JsNumber, JsObject, JsValue, Json}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

class SlickDAO(val games: TableQuery[Games], val cells: TableQuery[Cells], val inCupDice: TableQuery[InCupDice], val storedDice: TableQuery[StoredDice], val players: TableQuery[Players], val db: Database) extends IPersistence {
  def this() =
    this(
      TableQuery[Games],
      TableQuery[Cells],
      TableQuery[InCupDice],
      TableQuery[StoredDice],
      TableQuery[Players],
      Database.forConfig(path = "postgres")
    )

  private def getHighestGameId: Int = {
    val query = games.map(_.id).max
    val action = query.result

    val resultFuture: Future[Option[Int]] = db.run(action)
    val result: Int = Await.result(resultFuture.map {
      case Some(value) => value
      case None => 1
    }, Duration.Inf)
    result
  }

  private def createTablesIfNotExist(): Unit = {
    // DDL
    db.run(DBIO.seq(
      (games.schema ++ cells.schema ++ inCupDice.schema ++ storedDice.schema ++ players.schema).createIfNotExists,
    ))
  }

  override def saveDiceCup(diceCup: String): String = {
    createTablesIfNotExist()
    val inCupList = (Json.parse(diceCup) \ "dicecup" \ "incup").as[List[Int]]
    val lockedList = (Json.parse(diceCup) \ "dicecup" \ "stored").as[List[Int]]
    val remainingDices = (Json.parse(diceCup) \ "dicecup" \ "remainingDices").as[Int]

    val maxId: Int = getHighestGameId
    val updateAction = games.filter(_.id === maxId).map(g => g.remDice).update(remainingDices)
    db.run(updateAction)

    val deleteAction = storedDice.filter(_.gameId === maxId).delete
    db.run(deleteAction)

    val deleteAction2 = inCupDice.filter(_.gameId === maxId).delete
    db.run(deleteAction2)

    val insertActions =
      lockedList.map { (value: Int) =>
        (storedDice.map(s => (s.value, s.gameId)) returning storedDice.map(_.id)) += (value, maxId)
      } ++ inCupList.map { (value: Int) =>
        (inCupDice.map(s => (s.value, s.gameId)) returning inCupDice.map(_.id)) += (value, maxId)
      }

    val combinedAction = DBIO.sequence(insertActions).flatMap { _ =>
      DBIO.successful(())
    }
    val resultFuture: Future[Unit] = db.run(combinedAction)
    val result = Await.result(resultFuture
      .map(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(false)).toString), Duration.Inf
    )
    result
  }

  override def saveGame(game: String): String = {
    createTablesIfNotExist()
    val maxId: Int = getHighestGameId
    val remainingMoves = (Json.parse(game) \ "game" \ "remainingMoves").as[Int]

    val updateAction = games.filter(_.id === maxId).map(g => g.remMoves).update(remainingMoves)
    db.run(updateAction)

    val deleteAction = players.filter(_.gameId === maxId).delete
    db.run(deleteAction)


    val nestedList: Array[Array[String]] = (Json.parse(game) \ "game" \ "nestedList").as[String].split(";").map(elem => elem.split(","))
    val playersJson = (Json.parse(game) \ "game" \ "players").as[Array[JsObject]]
    val currentPlayerID = (Json.parse(game) \ "game" \ "currentPlayerID").as[Int]

    val insertActions =
      playersJson.map { playerJson =>
        val playerID = (playerJson \ "id").as[Int]
        val playerName = (playerJson \ "name").as[String]
        (players.map(p => (p.name, p.isTurn, p.top, p.bonus, p.sumTop, p.sumBottom, p.total, p.gameId)) returning players.map(_.id)) +=
          (playerName, playerID == currentPlayerID, nestedList(playersJson.indexOf(playerJson)).head.toInt,
            nestedList(playersJson.indexOf(playerJson))(1).toInt, nestedList(playersJson.indexOf(playerJson))(2).toInt,
            nestedList(playersJson.indexOf(playerJson))(3).toInt, nestedList(playersJson.indexOf(playerJson)).last.toInt, maxId)
      }

    val combinedAction = DBIO.sequence(insertActions).flatMap { _ =>
      DBIO.successful(())
    }
    val resultFuture: Future[Unit] = db.run(combinedAction)
    val result = Await.result(resultFuture
      .map(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(false)).toString), Duration.Inf
    )
    result
  }

  override def saveField(field: String): String = {
    createTablesIfNotExist()
    val maxId: Int = getHighestGameId

    val deleteAction = players.filter(_.gameId === maxId).delete
    db.run(deleteAction)

    val numberOfPlayers = (Json.parse(field) \ "field" \ "numberOfPlayers").as[Int]
    val rows = (Json.parse(field) \ "field" \ "rows").as[Array[JsValue]].map { outerValue =>
      outerValue.as[Array[JsValue]].map { innerVal =>
        innerVal.as[JsValue].match {
          case JsNumber(value) => Some(value.toInt)
          case _ => None
        }
      }.toList
    }.toList

    val insertActions = {
      (0 until numberOfPlayers).flatMap { col =>
        (0 until 19).map { row =>
          (cells.map(c => (c.x, c.y, c.value, c.gameId)) returning cells.map(_.id)) += (col, row, rows(row)(col), maxId)
        }
      }
    }

    val combinedAction = DBIO.sequence(insertActions).flatMap { _ =>
      DBIO.successful(())
    }
    val resultFuture: Future[Unit] = db.run(combinedAction)
    val result = Await.result(resultFuture
      .map(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(false)).toString), Duration.Inf
    )
    result
  }


  private def getInCupValuesByGameId(gameId: Int): List[Int] = {
    val selectAction = inCupDice.filter(_.gameId === gameId).map(_.value).result
    Await.result(db.run(selectAction).map(_.toList), Duration.Inf)
  }

  private def getStoredValuesByGameId(gameId: Int): List[Int] = {
    val selectAction = storedDice.filter(_.gameId === gameId).map(_.value).result
    Await.result(db.run(selectAction).map(_.toList), Duration.Inf)
  }

  override def loadDiceCup: String = {
    val maxId: Int = getHighestGameId
    val selectActionsGame = games.filter(_.id === maxId).map(g => g.remDice).sum

    val remDice: Int = Await.result(db.run(selectActionsGame.result).map {
      case Some(value) => value
      case None => 2
    }, Duration.Inf)

    Json.obj(
      "dicecup" -> Json.obj(
        "stored" -> getStoredValuesByGameId(maxId),
        "incup" -> getInCupValuesByGameId(maxId),
        "remainingDices" -> JsNumber(remDice)
      )
    ).toString
  }


  override def loadGame: String = {
    val maxId: Int = getHighestGameId

    val selectActionsGame = games.filter(_.id === maxId).map(g => g.remMoves).sum

    val remMoves: Int = Await.result(db.run(selectActionsGame.result).map {
      case Some(value) => value
      case None => 26
    }, Duration.Inf)

    val query = players
      .filter(_.gameId === maxId)
      .sortBy(p => p.name)
      .map(p => (p.name, p.isTurn, p.top, p.bonus, p.sumTop, p.sumBottom, p.total)).result

    val resultTuples: List[(String, Boolean, Int, Int, Int, Int, Int)] =
      Await.result(db.run(query).map(_.toList).recover(_ => List.empty), Duration.Inf)

    val resultNestedList: List[List[Int]] = resultTuples
      .map(p => List(p._3, p._4, p._5, p._6, p._5, p._7))
    
    val currentPlayer: (String, Int) = resultTuples.find(_._2)
      .map(p => (p._1, p._1.substring(6).toIntOption.getOrElse(1)))
      .getOrElse(("Player 1", 1))
      
    val playersList: JsArray = JsArray(resultTuples.map { p =>
      Json.obj(
        "id" -> JsNumber(p._1.substring(6).toIntOption.getOrElse(1)),
        "name" -> p._1
      )
    })  

    Json.obj(
      "game" -> Json.obj(
        "nestedList" -> resultNestedList.map(_.mkString(",")).mkString(";"),
        "remainingMoves" -> JsNumber(remMoves),
        "currentPlayerID" -> JsNumber(currentPlayer._2),
        "currentPlayerName" -> currentPlayer._1,
        "players" -> playersList
      )
    ).toString
  }

  override def loadField: String = {
    val maxId: Int = getHighestGameId
    val selectActions = cells.filter(_.gameId === maxId).map(g => g.x).max
    val numberOfPlayers: Int = Await.result(db.run(selectActions.result).map {
      case Some(value) => value + 1
      case None => 2
    }, Duration.Inf)

    val query = cells.filter(_.gameId === maxId).map(c => (c.x, c.y, c.value)).result
    val resultMap: Map[(Int, Int), Option[Int]] = Await.result(db.run(query).map(_.toList), Duration.Inf)
      .map {
        (key1, key2, value) => (key1, key2) -> value
      }.toMap

    val nestedVector: Vector[Vector[Option[Int]]] =
      (0 until 19).map { rows =>
        (0 until numberOfPlayers).map { cols =>
          resultMap((cols, rows))
        }.toVector
      }.toVector

    Json.obj(
      "field" -> Json.obj(
        "numberOfPlayers" -> JsNumber(numberOfPlayers),
        "rows" -> nestedVector
      )
    ).toString
  }

  override def createGame(numberOfPlayers: Int): String = {
    val insertGame = (games.map(g => (g.remMoves, g.remDice)) returning games.map(_.id)) += (13 * numberOfPlayers, 2)
    val insertFuture = db.run(insertGame)
    val result = Await.result(insertFuture.map(_ => "Inserted Game successfully").recover(_ => "Failure inserting Game"), Duration.Inf)
    result
  }
}
