package de.htwg.sa.kniffel.controller.util

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}

object HttpUtil:

  def sendRequest(baseURL: String, route: String, requestBody: String = ""): String =
    val url = new URL(baseURL + route)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    if (!requestBody.isBlank)
      connection.setRequestMethod("POST")
      connection.setDoOutput(true)
      connection.setRequestProperty("Content-Type", "application/json")

      val outputStreamWriter = new OutputStreamWriter(connection.getOutputStream, "UTF-8")
      outputStreamWriter.write(requestBody)
      outputStreamWriter.close()
    else
      connection.setRequestMethod("GET")
      connection.setDoOutput(true)
      connection.setRequestProperty("Content-Type", "application/json")

    if (connection.getResponseCode == HttpURLConnection.HTTP_OK)
      val streamReader = new InputStreamReader(connection.getInputStream)
      val reader = new BufferedReader(streamReader)
      val lines = Iterator.continually(reader.readLine()).takeWhile(_ != null)
      val response = lines.mkString("\n")

      reader.close()
      streamReader.close()

      response
    else
      throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode)


  def sendPUTRequest(baseURL: String, route: String): Unit =

    val url = new URL(baseURL + route)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    connection.setRequestMethod("PUT")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    if (connection.getResponseCode != HttpURLConnection.HTTP_OK)
      throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode)
