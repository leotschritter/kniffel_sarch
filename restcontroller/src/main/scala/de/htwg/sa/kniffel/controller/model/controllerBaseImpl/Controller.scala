package de.htwg.sa.kniffel.controller.model.controllerBaseImpl

import de.htwg.sa.kniffel.controller.entity.{DiceCup, Field, Game}
import de.htwg.sa.kniffel.controller.integration.dicecup.DiceCupESI
import de.htwg.sa.kniffel.controller.integration.field.FieldESI
import de.htwg.sa.kniffel.controller.integration.fileio.FileIOESI
import de.htwg.sa.kniffel.controller.integration.game.GameESI
import de.htwg.sa.kniffel.controller.model.IController
import de.htwg.sa.kniffel.controller.util.{Event, Move, UndoManager}
import play.api.libs.json
import play.api.libs.json.{JsObject, JsValue, Json}

class Controller (var field: Field, var diceCup: DiceCup, var game: Game, val diceCupESI: DiceCupESI,
                           val fieldESI: FieldESI, val gameESI: GameESI, val fileIOESI: FileIOESI) extends IController:
  def this(numberOfPlayers: Int) =
    this(new Field(numberOfPlayers), new DiceCup(), new Game(numberOfPlayers),
      new DiceCupESI(), new FieldESI(), new GameESI(), new FileIOESI())

  def this(field: Field, diceCup: DiceCup, game: Game) =
    this(field, diceCup, game,
      new DiceCupESI(), new FieldESI(), new GameESI(), new FileIOESI())


  val undoManager = new UndoManager[Game, Field]

  def undo(): String = {
    diceCup = nextRound()
    val r = undoManager.undoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    toString
  }

  def redo(): String = {
    diceCup = nextRound()
    val r = undoManager.redoStep(game, field)
    game = r._1
    field = r._2
    notifyObservers(Event.Move)
    toString
  }

  def put(move: Move): String = {
    diceCup = nextRound()
    val r = undoManager.doStep(game, field, SetCommand(move, gameESI, fieldESI))
    game = r._1
    field = r._2
    toString
  }

  def quit(): String = {
    notifyObservers(Event.Quit)
    toString
  }

  def next(): String =
    game = getNextGame
    toString
  
  def putOut(list: List[Int]): DiceCup =
    diceCupESI.sendRequest(s"diceCup/putOut/list=${list.mkString(",")}", diceCup.toJson.toString)


  def putIn(list: List[Int]): DiceCup =
    diceCupESI.sendRequest(s"diceCup/putIn/list=${list.mkString(",")}", diceCup.toJson.toString)

  // doAndPublish for nextRound() and dice()
  def doAndPublish(doThis: => DiceCup): String =
    diceCup = doThis
    notifyObservers(Event.Move)
    toString

  def dice(): DiceCup =
    diceCupESI.sendDiceRequest("diceCup/dice", diceCup.toJson.toString)
      .getOrElse(diceCup)

  def nextRound(): DiceCup =
    diceCupESI.sendRequest("diceCup/nextRound", diceCup.toJson.toString)

  def save(): String =
    fileIOESI.saveRequest("io/saveField", field.toJson.toString)
    fileIOESI.saveRequest("io/saveGame", game.toJson.toString)
    fileIOESI.saveRequest("io/saveDiceCup", diceCup.toJson.toString)
    notifyObservers(Event.Save)
    toString

  def load(): String =
    field = fileIOESI.loadFieldRequest
    game = fileIOESI.loadGameRequest
    diceCup = fileIOESI.loadDiceCupRequest
    notifyObservers(Event.Load)
    toString


  override def toString: String =
    s"${fieldESI.sendStringRequest("field/mesh", field.toJson.toString)}\n" +
      s"${diceCupESI.sendStringRequest("diceCup/representation", diceCup.toJson.toString)}\n$getPlayerName ist an der Reihe."

  override def toJson: JsObject =
    Json.obj(
      "controller" ->
        diceCup.toJson
          .deepMerge(field.toJson)
          .deepMerge(game.toJson))


  private def getNextGame: Game =
    gameESI.sendNextRequest(game)
      .getOrElse(game)

  def jsonStringToController(controller: String): IController =
    val controllerJson = Json.parse(controller)
    val f = Json.obj("field" -> (controllerJson \ "controller" \ "field").as[JsObject]).toString
    val dc = Json.obj("dicecup" -> (controllerJson \ "controller" \ "dicecup").as[JsObject]).toString
    val g = Json.obj("game" -> (controllerJson \ "controller" \ "game").as[JsObject]).toString
    new Controller(field.jsonStringToField(f), diceCup.jsonStringToDiceCup(dc), game.jsonStringToGame(g))

  override def jsonStringToMove(move: String): Move =
    Move(
      (Json.parse(move) \ "move" \ "value").as[Int],
      (Json.parse(move) \ "move" \ "x").as[Int],
      (Json.parse(move) \ "move" \ "y").as[Int]
    )

  private def getPlayerName: String =
    gameESI.sendPlayerNameRequest(game)

  override def writeDown(move: Move): String =
    put(move)
    next()
    doAndPublish(nextRound())
  