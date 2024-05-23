package de.htwg.sa.kniffel.persistence.persistence.mongoImpl

import de.htwg.sa.kniffel.persistence.persistence.IPersistence
import de.htwg.sa.kniffel.persistence.persistence.util.MongoDbDocumentConverter
import org.mongodb.scala.*
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Indexes.descending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.result.{DeleteResult, InsertOneResult, UpdateResult}
import play.api.libs.json.*

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

class MongoDAO(converter: MongoDbDocumentConverter) extends IPersistence {
  def this() = this(new MongoDbDocumentConverter())

  private val client = MongoClient("mongodb://localhost:27017/kniffeldbuser?authSource=kniffel")
  private val db: MongoDatabase = client.getDatabase("kniffeldb")
  private val gameCollection: MongoCollection[Document] = db.getCollection("game")
  private val fieldCollection: MongoCollection[Document] = db.getCollection("field")
  private val diceCupCollection: MongoCollection[Document] = db.getCollection("diceCup")


  override def loadField: String = loadField(getLatestGameId)

  override def loadGame: String = loadGame(getLatestGameId)

  override def loadDiceCup: String = loadDiceCup(getLatestGameId)

  override def loadField(gameId: Int): String =
    val document = Await.result(fieldCollection.find(equal("_id", gameId)).first().head(), Duration.Inf)
    converter.resultToFieldJson(document)

  override def loadGame(gameId: Int): String =
    val document = Await.result(gameCollection.find(equal("_id", gameId)).first().head(), Duration.Inf)
    converter.resultToGameJson(document)

  override def loadDiceCup(gameId: Int): String =
    val document = Await.result(diceCupCollection.find(equal("_id", gameId)).first().head(), Duration.Inf)
    converter.resultToDiceCupJson(document)

  override def saveField(field: String): String =
    val id = getLatestGameId
    deleteField(id)
    executeInsertStatement(fieldCollection.insertOne(converter.fieldToDocument(field, id)))

  override def saveGame(game: String): String = {
    val id = getLatestGameId
    executeUpdateGameStatement(id, converter.gameToDocument(game, id))
  }

  override def saveDiceCup(diceCup: String): String =
    val id: Int = getLatestGameId
    deleteDiceCup(id)
    executeInsertStatement(diceCupCollection.insertOne(converter.diceCupToDocument(diceCup, id)))

  override def deleteGame(gameId: Int): Unit =
    executeDeleteStatement(gameCollection.deleteOne(equal("_id", gameId)))

  override def deleteField(gameId: Int): Unit =
    executeDeleteStatement(fieldCollection.deleteOne(equal("_id", gameId)))

  override def deleteDiceCup(gameId: Int): Unit =
    executeDeleteStatement(diceCupCollection.deleteOne(equal("_id", gameId)))

  override def createGame(numberOfPlayers: Int): String = {
    val id: Int = Try(getLatestGameId).toOption.getOrElse(0) + 1
    val document = Document(
      "_id" -> id,
      "numberOfPlayers" -> numberOfPlayers
    )
    executeInsertStatement(gameCollection.insertOne(document))
  }

  override def loadOptions: String = {
    Await.result(gameCollection.find().map(_.get("_id").map(_.asInt32().getValue).getOrElse(0)).toFuture(), Duration.Inf)
      .mkString(",")
  }

  override def updateGame(game: String, gameId: Int): Unit = {
    val document = converter.gameToDocument(game, gameId)
    executeUpdateGameStatement(gameId, document)
  }

  override def updateField(field: String, gameId: Int): Unit =
    val document = converter.fieldToDocument(field, gameId)
    executeUpdateStatement(fieldCollection.updateOne(equal("_id", gameId), combine(
      set("rows",document("rows")),
      set("numberOfPlayers", document("numberOfPlayers"))
    )))

  override def updateDiceCup(diceCup: String, gameId: Int): Unit =
    val document = converter.diceCupToDocument(diceCup, gameId)
    executeUpdateStatement(diceCupCollection.updateOne(equal("_id", gameId), combine(
      set("remainingDices", document("remainingDices")),
      set("stored", document("stored")),
      set("incup", document("incup"))
    )))

  private def getLatestGameId: Int =
    val document = Await.result(gameCollection.find().sort(descending("_id")).first().head(), Duration.Inf)
    document.get("_id").map(_.asInt32().getValue).getOrElse(0)

  private def executeUpdateStatement(statement: SingleObservable[UpdateResult]) = {
    Await.result(statement
      .map(_ => Json.obj("updated successfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("updating failed" -> JsBoolean.apply(false)).toString)
      .head()
      , Duration.Inf)
  }

  private def executeDeleteStatement(statement: SingleObservable[DeleteResult]) = {
    Await.result(statement
      .map(_ => Json.obj("deleted successfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("deleting failed" -> JsBoolean.apply(false)).toString)
      .head()
      , Duration.Inf)
  }

  private def executeInsertStatement(statement: SingleObservable[InsertOneResult]) = {
    Await.result(statement
      .map(_ => Json.obj("saved successfully" -> JsBoolean.apply(true)).toString)
      .recover(_ => Json.obj("saving Failed" -> JsBoolean.apply(false)).toString)
      .head()
      , Duration.Inf)
  }

  private def executeUpdateGameStatement(gameId: Int, document: Document): String = {
    executeUpdateStatement(gameCollection.updateOne(equal("_id", gameId), combine(
      set("nestedList", document("nestedList")),
      set("remainingMoves", document("remainingMoves")),
      set("currentPlayerID", document("currentPlayerID")),
      set("currentPlayerName", document("currentPlayerName")),
      set("players", document("players"))
    )))
  }
}