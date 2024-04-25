package de.htwg.sa.kniffel.controller.integration.fileio

import de.htwg.sa.kniffel.controller.entity.{DiceCup, Field, Game}
import de.htwg.sa.kniffel.controller.util.HttpUtil


class FileIOESI: 

  val baseUrl = "http://localhost:9000/"

  def loadDiceCupRequest: DiceCup =
    new DiceCup().jsonStringToDiceCup(sendRequest("io/loadDiceCup"))
    
  def loadFieldRequest: Field =
    new Field(2).jsonStringToField(sendRequest("io/loadField"))
  
  def loadGameRequest: Game =
    new Game(2).jsonStringToGame(sendRequest("io/loadGame"))
    
  def saveRequest(path: String, requestBody: String = ""): Unit =
    sendRequest(path, requestBody)  

  private def sendRequest(path: String, requestBody: String = ""): String =
    HttpUtil.sendRequest(baseUrl, path, requestBody)

