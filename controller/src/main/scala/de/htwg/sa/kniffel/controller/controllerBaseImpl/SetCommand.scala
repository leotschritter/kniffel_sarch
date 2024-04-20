package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import de.htwg.sa.kniffel.game.IGame
import de.htwg.sa.kniffel.util.{Command, Move}

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}

class SetCommand(move: Move) extends Command[IGame, String]:

  override def doStep(game: IGame, field: String): (IGame, String) =
    val g = game.sum(move.value, move.y)
    (
      g,
      sendRequest(
        "field/putMulti/list=" + g.resultNestedList(move.x).mkString(",") + "/" + move.value + "/" + move.x + "/" + move.y,
        field
      )
    )

  override def redoStep(game: IGame, field: String): (IGame, String) =
    game.sum(move.value, move.y).next().map(g =>
    (
      g,
      sendRequest(
        "field/putMulti/list=" + g.resultNestedList(move.x).mkString(",") + "/" + move.value + "/" + move.x + "/" + move.y,
        field
      )
    )).getOrElse(game, field)
    /*game.sum(move.value.toInt, move.y).next()
      .match {
        case Some(g) => (g, field.putMulti(g.resultNestedList(move.x).map(_.toString), move.value, move.x, move.y))
        case None => (game, field)
      }*/

  override def undoStep(game: IGame, field: String): (IGame, String) =
    val g = game.undoMove(move.value, move.y)
    (
      g,
      sendRequest(
        "field/undoMove/list=" + g.resultNestedList(move.x).mkString(",") + "/" + move.x + "/" + move.y,
        field
      )
    )

  override def noStep(game: IGame, field: String): (IGame, String) =
    (game, field)


  def sendRequest(route: String, requestBody: String = ""): String = {
    val baseURL = "http://localhost:8080/"
    val url = new URL(baseURL + route)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    val outputStreamWriter = new OutputStreamWriter(connection.getOutputStream, "UTF-8")
    outputStreamWriter.write(requestBody)
    outputStreamWriter.close()

    if (connection.getResponseCode == HttpURLConnection.HTTP_OK) {
      val streamReader = new InputStreamReader(connection.getInputStream)
      val reader = new BufferedReader(streamReader)
      val lines = Iterator.continually(reader.readLine()).takeWhile(_ != null)
      val response = lines.mkString("\n")

      reader.close()
      streamReader.close()

      response
    } else {
      throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode)
    }
  }