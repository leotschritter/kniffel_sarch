package de.htwg.sa.kniffel.gui.integration.dicecup

import de.htwg.sa.kniffel.gui.util.HttpUtil

class DiceCupESI:
  val baseUrl: String = "http://localhost:9002/"

  def sendPOSTRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, "POST", requestBody)

  def sendGETRequest(path: String): String =
    HttpUtil.sendRequest(baseUrl, path, "GET")
