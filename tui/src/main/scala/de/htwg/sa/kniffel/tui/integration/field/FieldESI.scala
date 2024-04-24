package de.htwg.sa.kniffel.tui.integration.field

import de.htwg.sa.kniffel.tui.util.HttpUtil

class FieldESI:
  val baseUrl = "http://localhost:9001/"

  def sendPOSTRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, "POST", requestBody)

  def sendGETRequest(path: String): String =
    HttpUtil.sendRequest(baseUrl,  path, "GET")
  
