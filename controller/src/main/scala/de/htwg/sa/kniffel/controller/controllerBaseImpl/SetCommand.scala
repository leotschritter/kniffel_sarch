package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import de.htwg.sa.kniffel.util.HttpUtil.sendRequest
import de.htwg.sa.kniffel.util.{Command, Move}
import play.api.libs.json.{JsNull, JsValue, Json}

class SetCommand(move: Move) extends Command[String, String]:

  override def doStep(game: String, field: String): (String, String) =
    val g = sendRequest(s"game/sum/${move.value}/${move.y}", game)
    val list = getListAsString(g, move.x)
    (
      g,
      sendRequest(
        s"field/putMulti/list=$list/${move.value}/${move.x}/${move.y}",
        field
      )
    )

  override def redoStep(game: String, field: String): (String, String) =
    val g = sendRequest(s"game/sum/${move.value}/${move.y}", game)
    val nextG = getNextGame(g)
    val list = getListAsString(nextG, move.x)
    nextG.match {
      case "" => (game, field)
      case _ => (
        nextG,
        sendRequest(
          s"field/putMulti/list=$list/${move.value}/${move.x}/${move.y}",
          field
        )
      )
    }

  override def undoStep(game: String, field: String): (String, String) =
    val g = sendRequest(s"game/undoMove/${move.value}/${move.y}", game)
    val list = getListAsString(g, move.x)
    (
      g,
      sendRequest(
        s"field/undoMove/list=$list/${move.x}/${move.y}",
        field
      )
    )

  override def noStep(game: String, field: String): (String, String) =
    (game, field)

  private def getListAsString(game: String, x: Int): String = {
    (Json.parse(game) \ "game" \ "nestedList").as[String].split(";")(x)
  }

  private def getNextGame(game: String): String = {
    val nextGameString = sendRequest("game/next", game)
    val nextGameJson = Json.parse(nextGameString)
    (nextGameJson \ "game").as[JsValue].match {
      case JsNull => ""
      case _ => nextGameString
    }
  }