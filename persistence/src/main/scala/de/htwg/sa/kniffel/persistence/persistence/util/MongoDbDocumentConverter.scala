package de.htwg.sa.kniffel.persistence.persistence.util

import de.htwg.sa.kniffel.persistence.persistence.mongoImpl.model.Player
import org.mongodb.scala.Document
import play.api.libs.json.*

import scala.jdk.CollectionConverters.*
import scala.util.Try

class MongoDbDocumentConverter(converter: JsonConverter) {
  def this() = this(new JsonConverter())

  def fieldToDocument(field: String, id: Int): Document = {
    val json: JsValue = Json.parse(field)
    val jsonRows: JsArray = (json \ "field" \ "rows").get.as[JsArray]

    val rows: List[List[Option[Int]]] = jsonRows.value.map { row =>
      row.as[JsArray].value.map(cell => cell.asOpt[Int]
      ).toList
    }.toList

    Document(
      "_id" -> id,
      "numberOfPlayers" -> (json \ "field" \ "numberOfPlayers").as[Int],
      "rows" -> rows
    )
  }

  def diceCupToDocument(diceCup: String, id: Int): Document = {
    val remainingDices: Int = (Json.parse(diceCup) \ "dicecup" \ "remainingDices").as[Int]
    val stored: List[Int] = (Json.parse(diceCup) \ "dicecup" \ "stored").as[List[Int]]
    val inCup: List[Int] = (Json.parse(diceCup) \ "dicecup" \ "incup").as[List[Int]]

    Document(
      "_id" -> id,
      "remainingDices" -> remainingDices,
      "stored" -> stored,
      "incup" -> inCup
    )
  }

  def gameToDocument(game: String, id: Int): Document = {
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

    Document(
      "_id" -> id,
      "nestedList" -> nestedList,
      "remainingMoves" -> remainingMoves,
      "currentPlayerID" -> currentPlayerID,
      "currentPlayerName" -> currentPlayerName,
      "players" -> playersDocuments
    )
  }

  def resultToFieldJson(document: Document): String = {
    val rows: Vector[Vector[Option[Int]]] = document("rows").asArray().getValues.asScala.map { row =>
      row.asArray().getValues.asScala.map { cell => Try(cell.asInt32().getValue).toOption
      }.toVector
    }.toVector

    converter.fieldToJsonString(document("numberOfPlayers").asInt32().getValue, rows)
  }

  def resultToGameJson(document: Document): String = {
    println(document.toJson())
    val currentPlayer: Player = Player(
      document("currentPlayerID").asInt32().getValue,
      document("currentPlayerName").asString().getValue
    )

    val players: List[JsObject] = document("players").asArray().getValues.asScala.map { player =>
      Json.obj(
        "id" -> JsNumber(player.asDocument().get("id").asInt32().getValue),
        "name" -> player.asDocument().get("name").asString().getValue
      )
    }.toList

    val playersList: JsArray = JsArray(players)

    val resultNestedList: List[List[Int]] = document("nestedList").asArray().getValues.asScala.map { row =>
      row.asArray().getValues.asScala.map(_.asInt32().getValue).toList
    }.toList

    val res = Json.obj(
      "game" -> Json.obj(
        "nestedList" -> resultNestedList.map(_.mkString(",")).mkString(";"),
        "remainingMoves" -> JsNumber(document("remainingMoves").asInt32().getValue),
        "currentPlayerID" -> JsNumber(currentPlayer.id),
        "currentPlayerName" -> currentPlayer.name,
        "players" -> playersList
      )
    ).toString
    println("XXXXXXXXXXX" + res)
    res
  }

  def resultToDiceCupJson(document: Document): String = {
    converter.diceCupToJsonString(
      document("remainingDices").asInt32().getValue,
      document("stored").asArray().getValues.asScala.map(_.asInt32().getValue).toList,
      document("incup").asArray().getValues.asScala.map(_.asInt32().getValue).toList
    )
  }
}
