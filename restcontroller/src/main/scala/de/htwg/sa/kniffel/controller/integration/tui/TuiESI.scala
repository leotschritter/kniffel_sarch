package de.htwg.sa.kniffel.controller.integration.tui

import de.htwg.sa.kniffel.controller.util.HttpUtil

class TuiESI:

  val baseUrl = "http://localhost:9005/"

  def sendRequest(path: String): Unit =
    HttpUtil.sendPUTRequest(baseUrl, path)