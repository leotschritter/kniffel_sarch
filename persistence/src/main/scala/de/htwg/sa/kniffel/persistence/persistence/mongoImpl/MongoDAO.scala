package de.htwg.sa.kniffel.persistence.persistence.mongoImpl

import de.htwg.sa.kniffel.persistence.persistence.IPersistence
import de.htwg.sa.kniffel.persistence.persistence.mongoImpl.model.Player
import de.htwg.sa.kniffel.persistence.persistence.util.JsonConverter
import org.mongodb.scala.*
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Indexes.descending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.result.{DeleteResult, InsertOneResult, UpdateResult}
import play.api.libs.json.*

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.*

class MongoDAO(converter: JsonConverter) extends IPersistence {
  def this() = this(JsonConverter())


  private val client = MongoClient("mongodb://kniffeldbuser:kniffel@localhost:27017")
  private val db: MongoDatabase = client.getDatabase("kniffeldb")
  private val gameCollection: MongoCollection[Document] = db.getCollection("game")
  private val fieldCollection: MongoCollection[Document] = db.getCollection("field")
  private val diceCupCollection: MongoCollection[Document] = db.getCollection("diceCup")


  override def loadField: String = loadField(getLatestGameId)

  override def loadGame: String = loadGame(getLatestGameId)

  override def loadDiceCup: String = loadDiceCup(getLatestGameId)

  override def saveField(field: String): String = {
    val id = getLatestGameId
    deleteField(id)
    val json: JsValue = Json.parse(field)
    val jsonRows: JsArray = (json \ "field" \ "rows").get.as[JsArray]

    val rows: List[List[Option[Int]]] = jsonRows.value.map { row =>
      row.as[JsArray].value.map(cell => cell.asOpt[Int]
      ).toList
    }.toList

    val document = Document(
      "numberOfPlayers" -> (json \ "field" \ "numberOfPlayers").as[Int],
      "rows" -> rows
    )

    executeInsertStatement(fieldCollection.insertOne(document))
  }

  private def getLatestGameId: Int = {
    val document = Await.result(gameCollection.find().sort(descending("_id")).first().head(), Duration.Inf)
    document.get("_id").map(_.asInt32().getValue).getOrElse(0)
  }

  override def saveGame(game: String): String = {
    val json: JsValue = Json.parse(game)

    val nestedList: List[List[Int]] = (json \ "game" \ "nestedList").as[String].split(";").toList.map(_.split(",").toList.map(_.toInt))
    val remainingMoves: Int = (json \ "game" \ "remainingMoves").as[Int]
    val currentPlayerID: Int = (json \ "game" \ "currentPlayerID").as[Int]
    val currentPlayerName: String = (json \ "game" \ "currentPlayerName").as[String]

    val playersDocuments: List[Document] = (json \ "game" \ "players").as[List[JsObject]]
      .map { player =>
        Document(
          "id" -> (player \ "id").as[Int],
          "name" -> (player \ "name").as[String]
        )
      }

    executeUpdateStatement(gameCollection.updateOne(equal("_id", getLatestGameId),
      combine(set("nestedList", nestedList),
        set("remainingMoves", remainingMoves),
        set("currentPlayerID", currentPlayerID),
        set("currentPlayerName", currentPlayerName),
        set("players", playersDocuments))
    ))
  }

  override def saveDiceCup(diceCup: String): String = {
    val id: Int = getLatestGameId
    deleteDiceCup(id)

    val remainingDices: Int = (Json.parse(diceCup) \ "dicecup" \ "remainingDices").as[Int]
    val stored: List[Int] = (Json.parse(diceCup) \ "dicecup" \ "stored").as[List[Int]]
    val inCup: List[Int] = (Json.parse(diceCup) \ "dicecup" \ "incup").as[List[Int]]

    val document = Document(
      "_id" -> id,
      "remainingDices" -> remainingDices,
      "stored" -> stored,
      "incup" -> inCup
    )

    executeInsertStatement(diceCupCollection.insertOne(document))
  }

  override def createGame(numberOfPlayers: Int): String = {
    val id: Int = getLatestGameId + 1
    val document = Document(
      "_id" -> id,
      "numberOfPlayers" -> numberOfPlayers
    )

    executeInsertStatement(gameCollection.insertOne(document))
  }

  override def loadField(gameId: Int): String =
    val document = Await.result(fieldCollection.find(equal("_id", gameId)).first().head(), Duration.Inf)

    val rows: Vector[Vector[Int]] = document("rows").asArray().getValues.asScala.map { row =>
      row.asArray().getValues.asScala.map(_.asInt32().getValue).toVector
    }.toVector

    converter.fieldToJsonString(
      document("numberOfPlayers").asInt32().getValue,
      rows.map(_.map(Option.apply))
    )

  override def loadGame(gameId: Int): String = {
    val document = Await.result(gameCollection.find(equal("_id", gameId)).first().head(), Duration.Inf)

    val currentPlayer: Player = Player(
      document("currentPlayerID").asInt32().getValue,
      document("currentPlayerName").asString().getValue
    )

    val players: List[Player] = document("players").asArray().getValues.asScala.map { player =>
      Player(
        player.asDocument().get("id").asInt32().getValue,
        player.asDocument().get("name").asString().getValue
      )
    }.toList

    val resultNestedList: List[List[Int]] = document("nestedList").asString().getValue.split(";").map { stringList =>
      stringList.split(",").map(_.toInt).toList
    }.toList

    converter.gameToJsonString(
      document("remainingMoves").asInt32().getValue,
      players.map(player =>
        (
          player.name,
          player.id == currentPlayer.id,
          resultNestedList(player.id).head,
          resultNestedList(player.id - 1)(1),
          resultNestedList(player.id - 1)(2),
          resultNestedList(player.id - 1)(3),
          resultNestedList(player.id - 1).last)
      )
    )
  }

  override def loadDiceCup(gameId: Int): String =
    val document = Await.result(diceCupCollection.find(equal("_id", gameId)).first().head(), Duration.Inf)

    converter.diceCupToJsonString(
      document("remainingDices").asInt32().getValue,
      document("stored").asArray().getValues.asScala.map(_.asInt32().getValue).toList,
      document("incup").asArray().getValues.asScala.map(_.asInt32().getValue).toList
    )

  override def loadOptions: String = {
    Await.result(gameCollection.find().map(_.get("_id").map(_.asInt32().getValue).getOrElse(0)).toFuture(), Duration.Inf)
      .mkString(",")
  }

  override def deleteGame(gameId: Int): Unit =
    executeDeleteStatement(gameCollection.deleteOne(equal("_id", gameId)))

  override def deleteField(gameId: Int): Unit =
    executeDeleteStatement(fieldCollection.deleteOne(equal("_id", gameId)))

  override def deleteDiceCup(gameId: Int): Unit =
    executeDeleteStatement(diceCupCollection.deleteOne(equal("_id", gameId)))

  private def executeInsertStatement(statement: SingleObservable[InsertOneResult]) = {
    Await.result(statement
      .map(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("savedSuccessfully" -> JsBoolean.apply(false)).toString)
      .head()
      , Duration.Inf)
  }

  private def executeUpdateStatement(statement: SingleObservable[UpdateResult]) = {
    Await.result(statement
      .map(_ => Json.obj("updatedSuccessfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("updatedSuccessfully" -> JsBoolean.apply(false)).toString)
      .head()
      , Duration.Inf)
  }

  private def executeDeleteStatement(statement: SingleObservable[DeleteResult]) = {
    Await.result(statement
      .map(_ => Json.obj("deletedSuccessfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("deletedSuccessfully" -> JsBoolean.apply(false)).toString)
      .head()
      , Duration.Inf)
  }

  override def updateGame(game: String, gameId: Int): Unit = ???

  override def updateField(field: String, gameId: Int): Unit = ???

  override def updateDiceCup(diceCup: String, gameId: Int): Unit = ???
}
