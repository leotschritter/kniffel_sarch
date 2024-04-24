package de.htwg.sa.kniffel.controller.model.controllerBaseImpl

import de.htwg.sa.kniffel.controller.entity.{Field, Game}
import de.htwg.sa.kniffel.controller.integration.field.FieldESI
import de.htwg.sa.kniffel.controller.integration.game.GameESI
import de.htwg.sa.kniffel.controller.util.{Command, Move}
import play.api.libs.json.Json

import scala.util.Try

class SetCommand(move: Move, val gameESI: GameESI, val fieldESI: FieldESI) extends Command[Game, Field]:
  def this(move: Move) = this(move, GameESI(), FieldESI())


  override def doStep(game: Game, field: Field): (Game, Field) =
    val g: Game = gameESI.sendRequest(s"game/sum/${move.value}/${move.y}", game.toJson.toString)
    val list = g.nestedList(move.x).mkString(",")
    val f = fieldESI.sendRequest(s"field/putMulti/list=$list/${move.value}/${move.x}/${move.y}",
      field.toJson.toString)
    (g, f)


  override def redoStep(game: Game, field: Field): (Game, Field) =
    val g = gameESI.sendRequest(s"game/sum/${move.value}/${move.y}", game.toJson.toString)
    val nextG = getNextGame(g)
    nextG.match {
      case None => (game, field)
      case Some(nextG) =>
        val list = nextG.nestedList(move.x).mkString(",")
        (
        nextG,
          fieldESI.sendRequest(s"field/putMulti/list=$list/${move.value}/${move.x}/${move.y}",
            field.toJson.toString
        )
          )
    }

  private def getListAsString(game: String, x: Int): String = {
    (Json.parse(game) \ "game" \ "nestedList").as[String].split(";")(x)
  }

  override def undoStep(game: Game, field: Field): (Game, Field) =
    val g = gameESI.sendRequest(s"game/undoMove/${move.value}/${move.y}", game.toJson.toString)
    val list = g.nestedList(move.x).mkString(",")
    (
      g,
      fieldESI.sendRequest(s"field/undoMove/list=$list/${move.x}/${move.y}",field.toJson.toString)
    )

  override def noStep(game: Game, field: Field): (Game, Field) =
    (game, field)


  private def getNextGame(game: Game): Option[Game] = {
    gameESI.sendNextRequest(game)

  }