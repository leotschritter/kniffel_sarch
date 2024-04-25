package de.htwg.sa.kniffel.controller.integration.field

import de.htwg.sa.kniffel.controller.entity.Field
import de.htwg.sa.kniffel.controller.util.HttpUtil

import scala.util.Try

class FieldESI:
  val baseUrl = "http://localhost:9001/"

  def sendRequest(path: String, requestBody: String = ""): Field =
    new Field(2).jsonStringToField(HttpUtil.sendRequest(baseUrl, path, requestBody))

  def sendNextRequest(path: String, requestBody: String = ""): Option[Field] =
    Try(new Field(2).jsonStringToField(HttpUtil.sendRequest(baseUrl, path, requestBody))).toOption
    
  def sendStringRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, requestBody)