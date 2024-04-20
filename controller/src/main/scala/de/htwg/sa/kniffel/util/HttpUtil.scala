package de.htwg.sa.kniffel.util

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}

object HttpUtil {
  def sendRequest(route: String, requestBody: String = ""): String = {
    val baseURL = "http://localhost:8080/"
    val url = new URL(baseURL + route)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    val outputStreamWriter = new OutputStreamWriter(connection.getOutputStream, "UTF-8")
    outputStreamWriter.write(requestBody)
    outputStreamWriter.close()

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
}
