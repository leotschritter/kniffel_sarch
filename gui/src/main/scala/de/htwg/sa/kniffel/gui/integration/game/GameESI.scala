package de.htwg.sa.kniffel.gui.integration.game

import de.htwg.sa.kniffel.gui.util.HttpUtil

class GameESI:

  val baseUrl = "http://localhost:9003/"

  def sendPOSTRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl,  path, "POST", requestBody)
    
  def sendGETRequest(path: String): String =
    HttpUtil.sendRequest(baseUrl,  path, "GET")
      
