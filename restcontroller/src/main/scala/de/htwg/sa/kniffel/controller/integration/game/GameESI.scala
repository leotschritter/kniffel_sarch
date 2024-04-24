package de.htwg.sa.kniffel.controller.integration.game

import de.htwg.sa.kniffel.controller.entity.Game
import de.htwg.sa.kniffel.controller.util.HttpUtil
import play.api.libs.json.Json

import scala.util.Try

class GameESI:
  val baseUrl = "http://localhost:9003/"

  def sendRequest(path: String, requestBody: String = ""): Game =
    new Game(2).jsonStringToGame(sendStringRequest(path, requestBody))

  def sendNextRequest(requestBody: Game): Option[Game] =
    Try(new Game(2).jsonStringToGame(sendStringRequest("game/next", requestBody.toJson.toString))).toOption

  def sendPlayerIDRequest(game: Game): Int =
    (Json.parse(sendStringRequest("game/playerID", game.toJson.toString)) \ "playerID").as[Int]

  def sendPlayerNameRequest(game: Game): String =
    (Json.parse(sendStringRequest("game/playerName", game.toJson.toString)) \ "playerName").as[String]


  def sendStringRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, requestBody)