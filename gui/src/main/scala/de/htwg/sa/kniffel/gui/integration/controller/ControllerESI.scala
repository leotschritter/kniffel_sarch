package de.htwg.sa.kniffel.gui.integration.controller

import de.htwg.sa.kniffel.gui.util.HttpUtil

class ControllerESI:
  val baseUrl: String = "http://localhost:9006/"

  def sendPOSTRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, "POST", requestBody)

  def sendGETRequest(path: String): String =
    HttpUtil.sendRequest(baseUrl, path, "GET")






