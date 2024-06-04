package de.htwg.sa.kniffel.game.model.gameBaseImpl

import play.api.libs.json.*
import akka.http.scaladsl.server.Directives.*
import de.htwg.sa.kniffel.game.model.IGame

case class Game(playersList: List[Player], currentPlayer: Player, remainingMoves: Int, resultNestedList: List[List[Int]]) extends IGame :
  def this(numberOfPlayers: Int) = this(
    (0 until numberOfPlayers).map(s => Player(s, "Player " + (s + 1))).toList,
    Player(0, "Player 1"),
    numberOfPlayers * 13,
    List.fill(numberOfPlayers, 6)(0)
  )

  def next(): Option[Game] = {
    if (remainingMoves == 0) None
    else Some(copy(currentPlayer = nextPlayer, remainingMoves = remainingMoves - 1))
  }

  private def previousPlayer: Player = {
    val prevIndex = (playersList.indexOf(currentPlayer) - 1 + playersList.size) % playersList.size
    playersList(prevIndex)
  }


  private def nextPlayer: Player =
    playersList((playersList.indexOf(currentPlayer) + 1) % playersList.length)

  private def sums(value: Int, y: Int, player: Player): (Int, Int, Int) = {
    val sumTop: Int = if y < 6 then value + nestedList(playersList.indexOf(player)).head else
      nestedList(playersList.indexOf(player)).head
    val sumBottom: Int = if y > 8 then value + nestedList(playersList.indexOf(player))(3) else
      nestedList(playersList.indexOf(player))(3)
    val bonus: Int = if sumTop >= 63 then 35 else 0
    (sumTop, sumBottom, bonus)
  }

  def sum(value: Int, y: Int): Game = {
    val (sumTop, sumBottom, bonus) = sums(value, y, currentPlayer)
    val updatedNestedList = List(sumTop, bonus, sumTop + bonus, sumBottom, sumTop + bonus, sumBottom + sumTop + bonus)
    copy(resultNestedList = resultNestedList.updated(playersList.indexOf(currentPlayer), updatedNestedList))

  }



  def undoMove(value: Int, y: Int): Game = {
    val (sumTop, sumBottom, bonus) = sums(-value, y, previousPlayer)
    val updatedNestedList = List(sumTop, bonus, sumTop + bonus, sumBottom, sumTop + bonus, sumBottom + sumTop + bonus)
    copy(resultNestedList = resultNestedList.updated(playersList.indexOf(previousPlayer), updatedNestedList), remainingMoves = remainingMoves + 1)
  }


  def playerID: Int = currentPlayer.playerID

  def playerName: String = currentPlayer.playerName

  def playerName(x: Int): String = playersList(x).playerName

  def nestedList: List[List[Int]] = resultNestedList

  def playerTuples: List[(Int, String)] =
    playersList.map(player => (player.playerID, player.playerName))

  override def toJson: JsObject = {
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



  override def jsonStringToGame(game: String): IGame = {
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

  override def newGame(players: Int): IGame = new Game(players)