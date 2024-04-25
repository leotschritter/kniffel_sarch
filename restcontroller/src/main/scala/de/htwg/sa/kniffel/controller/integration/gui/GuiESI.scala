package de.htwg.sa.kniffel.controller.integration.gui

import de.htwg.sa.kniffel.controller.util.HttpUtil

import java.net.{HttpURLConnection, URL}

class GuiESI:
  val baseUrl = "http://localhost:9004/"

  def sendRequest(path: String): Unit =
    HttpUtil.sendPUTRequest(baseUrl, path)