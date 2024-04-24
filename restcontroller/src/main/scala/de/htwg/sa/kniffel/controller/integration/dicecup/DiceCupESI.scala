package de.htwg.sa.kniffel.controller.integration.dicecup

import de.htwg.sa.kniffel.controller.entity.DiceCup
import de.htwg.sa.kniffel.controller.util.HttpUtil
import de.htwg.sa.kniffel.controller.util.HttpUtil.sendRequest
import play.api.libs.json.Json

import scala.util.Try

class DiceCupESI:

  val baseUrl: String = "http://localhost:9002/"

  def sendRequest(path: String, requestBody: String = ""): DiceCup =
    new DiceCup().jsonStringToDiceCup(sendStringRequest(path, requestBody))

  def sendDiceRequest(path: String, requestBody: String = ""): Option[DiceCup] =
    Try(new DiceCup().jsonStringToDiceCup(sendStringRequest(path, requestBody))).toOption

  def sendIndexOfFieldRequest(value: String): Int =
    (Json.parse(sendStringRequest("diceCup/indexOfField")) \ "indexOfField" \ value).as[Int]

  def sendResultRequest(index: Int, diceCup: DiceCup): Int =
    (Json.parse(sendStringRequest(s"diceCup/result/$index", diceCup.toJson.toString)) \ "result").as[Int]

  def sendStringRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, requestBody)