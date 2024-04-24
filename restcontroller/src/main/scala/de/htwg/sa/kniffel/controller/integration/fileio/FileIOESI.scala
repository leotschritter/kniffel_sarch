package de.htwg.sa.kniffel.controller.integration.fileio

import de.htwg.sa.kniffel.controller.entity.{DiceCup, Field, Game}
import de.htwg.sa.kniffel.controller.util.HttpUtil


class FileIOESI: 

  val baseUrl = "http://localhost:9000/"

  def loadDiceCupRequest(path: String, requestBody: String = ""): DiceCup =
    new DiceCup().jsonStringToDiceCup(sendRequest(path, requestBody))
    
  def loadFieldRequest(path: String, requestBody: String = ""): Field =
    new Field(2).jsonStringToField(sendRequest(path, requestBody))
  
  def loadGameRequest(path: String, requestBody: String = ""): Game =
    new Game(2).jsonStringToGame(sendRequest(path, requestBody))
    
  def saveRequest(path: String, requestBody: String = ""): Unit =
    sendRequest(path, requestBody)  

  private def sendRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, requestBody)

