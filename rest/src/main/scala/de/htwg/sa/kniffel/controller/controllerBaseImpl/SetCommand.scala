package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import de.htwg.sa.kniffel.field.IField
import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.util.HttpUtil.sendRequest
import de.htwg.sa.kniffel.util.{Command, Move}
import play.api.libs.json.Json

import scala.util.Try

class SetCommand(move: Move) extends Command[IGame, IField]:


  override def doStep(game: IGame, field: IField): (IGame, IField) =
    val g: IGame = game.jsonStringToGame(sendRequest(s"game/sum/${move.value}/${move.y}", game.toJson.toString))
    val list = g.nestedList(move.x).mkString(",")
    val f = field.jsonStringToField(sendRequest(s"field/putMulti/list=$list/${move.value}/${move.x}/${move.y}",
      field.toJson.toString))
    (g, f)


  override def redoStep(game: IGame, field: IField): (IGame, IField) =
    val g = game.jsonStringToGame(sendRequest(s"game/sum/${move.value}/${move.y}", game.toJson.toString))
    val nextG = getNextGame(g)
    nextG.match {
      case None => (game, field)
      case Some(nextG) =>
        val list = nextG.nestedList(move.x).mkString(",")
        (
        nextG,
          field.jsonStringToField(sendRequest(
          s"field/putMulti/list=$list/${move.value}/${move.x}/${move.y}",
            field.toJson.toString
        )
          ))
    }

  private def getListAsString(game: String, x: Int): String = {
    (Json.parse(game) \ "game" \ "nestedList").as[String].split(";")(x)
  }

  override def undoStep(game: IGame, field: IField): (IGame, IField) =
    val g = game.jsonStringToGame(sendRequest(s"game/undoMove/${move.value}/${move.y}", game.toJson.toString))
    val list = g.nestedList(move.x).mkString(",")
    (
      g,
      field.jsonStringToField(sendRequest(
        s"field/undoMove/list=$list/${move.x}/${move.y}",
        field.toJson.toString
      ))
    )

  override def noStep(game: IGame, field: IField): (IGame, IField) =
    (game, field)


  private def getNextGame(game: IGame): Option[IGame] = {
    Try(game.jsonStringToGame(sendRequest("game/next", game.toJson.toString))).toOption

  }