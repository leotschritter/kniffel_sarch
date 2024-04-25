package de.htwg.sa.kniffel.gui.util

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}

object HttpUtil {
  
  def sendRequest(baseUrl: String, route: String, requestMethod: String, requestBody: String = ""): String = {
    
    val url = new URL(baseUrl + route)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    if (requestMethod.equals("POST")) {
      connection.setRequestMethod("POST")
      connection.setDoOutput(true)
      connection.setRequestProperty("Content-Type", "application/json")

      val outputStreamWriter = new OutputStreamWriter(connection.getOutputStream, "UTF-8")
      outputStreamWriter.write(requestBody)
      outputStreamWriter.close()
    } else if (requestMethod.equals("GET")) {
      connection.setRequestMethod("GET")
      connection.setDoOutput(true)
      connection.setRequestProperty("Content-Type", "application/json")
    }

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
