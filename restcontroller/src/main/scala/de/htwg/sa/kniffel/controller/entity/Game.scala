package de.htwg.sa.kniffel.controller.entity

import play.api.libs.json.{JsArray, JsNumber, JsObject, JsValue, Json}

import scala.annotation.tailrec

case class Game(playersList: List[Player], currentPlayer: Player, remainingMoves: Int, resultNestedList: List[List[Int]]):
  def this(numberOfPlayers: Int) = this(
    (0 until numberOfPlayers).map(s => Player(s, "Player " + (s + 1))).toList,
    Player(0, "Player 1"),
    numberOfPlayers * 13,
    List.fill(numberOfPlayers, 6)(0)
  )

  def toJson: JsObject = {
    Json.obj(
      "game" -> Json.obj(
        "nestedList" -> this.nestedList.map(_.mkString(",")).mkString(";"),
        "remainingMoves" -> JsNumber(this.remainingMoves),
        "currentPlayerID" -> JsNumber(this.playerID),
        "currentPlayerName" -> this.playerName,
        "players" -> JsArray(
          this.playerTuples.map { playerTuple =>
            Json.obj(
              "id" -> JsNumber(playerTuple._1),
              "name" -> playerTuple._2
            )
          }
        )
      )
    )
  }

  def jsonStringToGame(game: String): Game = {
    val json: JsValue = Json.parse(game)
    val currPlayer: Player = Player((json \ "game" \ "currentPlayerID").get.toString.toInt, (json \ "game" \ "currentPlayerName").as[String])
    val remMoves: Int = (json \ "game" \ "remainingMoves").get.toString.toInt

    val jsonPlayers: JsArray = (json \ "game" \ "players").as[JsArray]
    val players: List[Player] = jsonPlayers.value.map { player =>
      Player(
        (player \ "id").as[Int],
        (player \ "name").as[String]
      )
    }.toList

    val jsonNestedList: String = (json \ "game" \ "nestedList").as[String]
    val resNestedList: List[List[Int]] = jsonNestedList.split(";").map { (stringList: String) =>
      stringList.split(",").map(_.toInt).toList
    }.toList

    Game(players, currPlayer, remMoves, resNestedList)
  }

  def playerID: Int = currentPlayer.playerID

  def playerName: String = currentPlayer.playerName

  def playerName(x: Int): String = playersList(x).playerName

  def nestedList: List[List[Int]] = resultNestedList

  private def playerTuples: List[(Int, String)] =
    @tailrec
    def playerTuplesHelper(players: List[Player], acc: List[(Int, String)]): List[(Int, String)] = players match
      case Nil => acc.reverse
      case player :: rest => playerTuplesHelper(rest, (player.playerID, player.playerName) :: acc)

    playerTuplesHelper(playersList, Nil)

