package de.htwg.sa.kniffel.tui

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.*
import play.api.libs.json.{JsNumber, JsObject, Json}

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class TUI:
  var continue = true

  def run(): Unit =
    println(sendRequest("field/mesh", sendRequest("controller/field")))
    inputLoop()

  def update(event: String): String =
    event match {
      case "quit" => continue = false; Json.obj("event" -> event).toString
      case "save" => continue; Json.obj("event" -> event).toString
      case _ => println(sendRequest("controller/")); Json.obj("event" -> event).toString
    }


  def inputLoop(): Unit =
    analyseInput(readLine) match
      case None => inputLoop()
      case Some(move) => writeDown(move)
    if continue then inputLoop()


  def analyseInput(input: String): Option[String] =
    val textInputAsList = input.split("\\s").toList
    textInputAsList.head match
      case "q" => None
      case "po" => diceCupPutOut(textInputAsList.tail.map(_.toInt)); None
      case "pi" => diceCupPutIn(textInputAsList.tail.map(_.toInt)); None
      case "d" => sendRequest("controller/doAndPublish/dice"); None
      case "u" => sendRequest("controller/undo"); None
      case "r" => sendRequest("controller/redo"); None
      case "s" => sendRequest("controller/save"); None
      case "l" => sendRequest("controller/load"); None
      case "wd" =>
        validInput(textInputAsList) match {
          case Success(f) => val posAndDesc = textInputAsList.tail.head
            getIndexOfField(posAndDesc)
              .match {
                case Some(index) =>
                  if (checkIfEmpty(index))
                    Some(moveToJson(getResult(index), getPlayerID, index).toString)
                  else
                    println("Da steht schon was!")
                    None
                case None => println("Falsche Eingabe!"); None
              }
          case Failure(v) => println("Falsche Eingabe"); None
        }
      case _ =>
        println("Falsche Eingabe!"); None

  def validInput(list: List[String]): Try[String] = Try(list.tail.head)

  def writeDown(move: String): Unit = {
    sendRequest("controller/put", move)
    sendRequest("controller/next")
    sendRequest("controller/doAndPublish/nextRound")
  }

  private def diceCupPutIn(pi: List[Int]): Unit = sendRequest(s"controller/doAndPublish/putIn/list=${pi.mkString(",")}")

  private def diceCupPutOut(po: List[Int]): Unit = sendRequest(s"controller/doAndPublish/putOut/list=${po.mkString(",")}")

  private def checkIfEmpty(index: Int): Boolean = {
    (Json.parse(
      sendRequest(
        s"field/isEmpty/$getPlayerID/$index",
        sendRequest("controller/field")
      )
    ) \ "isEmpty").as[Boolean]
  }

  private def getPlayerName: String = {
    (Json.parse(sendRequest("game/playerName", sendRequest("controller/game"))) \ "playerName").as[String]
  }

  private def getPlayerID: Int = {
    (Json.parse(sendRequest("game/playerID", sendRequest("controller/game"))) \ "playerID").as[Int]
  }

  private def getIndexOfField(posAndDesc: String): Option[Int] = {
    try {
      Some((Json.parse(sendRequest("diceCup/indexOfField")) \ "indexOfField" \ posAndDesc).as[Int])
    } catch {
      case e: Throwable => None
    }
  }

  private def getResult(index: Int): Int = {
    (Json.parse(sendRequest(s"diceCup/result/$index", sendRequest("controller/diceCup"))) \ "result").as[Int]
  }

  private def moveToJson(value: Int, x: Int, y: Int): JsObject = {
    Json.obj(
      "move" -> Json.obj(
        "value" -> JsNumber(value),
        "x" -> JsNumber(x),
        "y" -> JsNumber(y)
      )
    )
  }

  private def sendRequest(route: String, requestBody: String = ""): String = {
    val baseURL = "http://localhost:8080/"
    val url = new URL(baseURL + route)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    if (!requestBody.isBlank) {
      connection.setRequestMethod("POST")
      connection.setDoOutput(true)
      connection.setRequestProperty("Content-Type", "application/json")

      val outputStreamWriter = new OutputStreamWriter(connection.getOutputStream, "UTF-8")
      outputStreamWriter.write(requestBody)
      outputStreamWriter.close()
    } else {
      connection.setRequestMethod("GET")
      connection.setDoOutput(true)
      connection.setRequestProperty("Content-Type", "application/json")
    }

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

  val tuiRoute: Route =
    concat(
      put {
        concat(
          path("quit") {
            complete(update("quit"))
          },
          path("save") {
            complete(update("save"))
          },
          path("load") {
            complete(update("load"))
          },
          path("move") {
            complete(update("move"))
          },
        )
      }
    )