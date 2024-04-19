package de.htwg.sa.kniffel.game.gameBaseImpl

import de.htwg.sa.kniffel.game.IGame
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsValue, Json}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

import scala.annotation.tailrec

case class Game(playersList: List[Player], currentPlayer: Player, remainingMoves: Int, resultNestedList: List[List[Int]]) extends IGame :
  def this(numberOfPlayers: Int) = this(
    (0 until numberOfPlayers).map(s => Player(s, "Player " + (s + 1))).toList,
    Player(0, "Player 1"),
    numberOfPlayers * 13,
    List.fill(numberOfPlayers, 6)(0)
  )

  def next(): Option[Game] = {
    if (remainingMoves == 0)
      None
    else
      Some(Game(playersList, nextPlayer, remainingMoves - 1, resultNestedList))
  }

  private def previousPlayer: Player = {
    if (playersList.indexOf(currentPlayer) - 1 < 0)
      playersList(playersList.last.playerID)
    else
      playersList(playersList.indexOf(currentPlayer) - 1)
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
    Game(playersList, currentPlayer, remainingMoves, resultNestedList.updated(
      playersList.indexOf(currentPlayer),
      List(sumTop) :+ bonus :+ (sumTop + bonus) :+ sumBottom :+ (sumTop + bonus) :+ (sumBottom + sumTop + bonus)
    ))
  }

  def undoMove(value: Int, y: Int): Game = {
    val (sumTop, sumBottom, bonus) = sums(-value, y, previousPlayer)
    Game(playersList, previousPlayer, remainingMoves + 1, resultNestedList.updated(
      playersList.indexOf(previousPlayer),
      List(sumTop) :+ bonus :+ (sumTop + bonus) :+ sumBottom :+ (sumTop + bonus) :+ (sumBottom + sumTop + bonus)
    ))
  }


  def playerID: Int = currentPlayer.playerID

  def playerName: String = currentPlayer.playerName

  def playerName(x: Int): String = playersList(x).playerName

  def nestedList: List[List[Int]] = resultNestedList


  def playerTuples: List[(Int, String)] =
    @tailrec
    def playerTuplesHelper(players: List[Player], acc: List[(Int, String)]): List[(Int, String)] = players match
      case Nil => acc.reverse
      case player :: rest => playerTuplesHelper(rest, (player.playerID, player.playerName) :: acc)
    playerTuplesHelper(playersList, Nil)

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

  override val gameRoute: Route =
    concat(
      get {
        concat(
          path("playerID") {
            complete(Json.obj(
              "playerID" -> JsNumber(this.playerID)
            ).toString)
          },
          path("playerName") {
            complete(Json.obj(
              "playerName" -> this.playerName
            ).toString)
          },
          path("playerName" / IntNumber) { (x: Int) =>
            complete(Json.obj(
              "playerName" -> this.playerName(x)
            ).toString)
          },
          path("nestedList") {
            complete(Json.obj(
              "nestedList" -> this.nestedList.map(_.mkString(",")).mkString(";")
            ).toString)
          },
          path("remainingMoves") {
            complete(Json.obj(
              "remainingMoves" -> JsNumber(this.remainingMoves)
            ).toString)
          },
          path("players") {
            complete(Json.obj(
              "players" -> Json.toJson(
                Seq(for {
                  x <- this.playerTuples
                } yield {
                  Json.obj(
                    "id" -> JsNumber(x._1),
                    "name" -> x._2)
                })
              )
            ).toString)
          },
          path("") {
            sys.error("No such GET route")
          }
        )
      },
      post {
        concat(
          path("next") {
            complete(this.next()
              .match {
                case Some(game) => game.toJson.toString
                case None => "{}"
              }
            )
          },
          path("undoMove" / IntNumber / IntNumber) { (value: Int, y: Int) =>
            complete(this.undoMove(value, y).toJson.toString)
          },
          path("sum" / IntNumber / IntNumber) { (value: Int, y: Int) =>
            complete(this.sum(value, y).toJson.toString)
          },
          path("") {
            sys.error("No such POST route")
          }
        )
      }
    )

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
