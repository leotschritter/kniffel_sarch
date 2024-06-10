package de.htwg.sa.kniffel.tui.aview

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.{FlowShape, Graph}
import de.htwg.sa.kniffel.tui.integration.controller.ControllerESI
import de.htwg.sa.kniffel.tui.integration.dicecup.DiceCupESI
import de.htwg.sa.kniffel.tui.integration.field.FieldESI
import de.htwg.sa.kniffel.tui.integration.game.GameESI
import play.api.libs.json.{JsNumber, JsObject, Json}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class TUI(val gameESI: GameESI, val diceCupESI: DiceCupESI, val fieldESI: FieldESI, val controllerESI: ControllerESI):
  def this() = this(GameESI(), DiceCupESI(), FieldESI(), ControllerESI())

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher
  var continue = true

  def run(): Unit =
    println(fieldESI.sendPOSTRequest("field/mesh", controllerESI.sendGETRequest("controller/field")))
    inputLoop()

  def update(event: String): String =
    event match {
      case "quit" => continue = false; Json.obj("event" -> event).toString
      case "save" => continue; Json.obj("event" -> event).toString
      case _ => println(controllerESI.sendGETRequest("controller/")); Json.obj("event" -> event).toString
    }


  def inputLoop(): Unit =
    analyseInput(readLine) match
      case None => inputLoop()
      case Some(move) => writeDown(move)
    if continue then inputLoop()

  private def analyseInputGraph: Graph[FlowShape[List[String], Option[String]], NotUsed] = GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*

    val broadcast = builder.add(Broadcast[List[String]](1))
    val merge = builder.add(Merge[Option[String]](1))

    val inputFlowShape = builder.add(
      Flow[List[String]].map { textInput =>
        textInput.head match
          case "q" => None
          case "po" => diceCupPutOut(textInput.tail.map(_.toInt)); None
          case "pi" => diceCupPutIn(textInput.tail.map(_.toInt)); None
          case "d" => controllerESI.sendGETRequest("controller/doAndPublish/dice"); None
          case "u" => controllerESI.sendGETRequest("controller/undo"); None
          case "r" => controllerESI.sendGETRequest("controller/redo"); None
          case "s" => controllerESI.sendGETRequest("controller/save"); None
          case "lo" => println(controllerESI.sendGETRequest("controller/loadOptions")); None
          case "l" =>
            validInput(textInput) match {
              case Success(f) => controllerESI.sendGETRequest("controller/load/"
                + Try(textInput.tail.head.toInt).toOption.getOrElse(1));
                None
              case Failure(v) => controllerESI.sendGETRequest("controller/load"); None
            }
          case "wd" =>
            validInput(textInput) match {
              case Success(f) => val posAndDesc = textInput.tail.head
                getIndexOfField(posAndDesc)
                  .match {
                    case Some(index) =>
                      if (checkIfEmpty(index))
                        Some(moveToJson(getResult(index), getPlayerID, index).toString)
                      else
                        println("Da steht schon was!")
                        None
                    case None => println("Falsche Eingabe!"); None
                  }
              case Failure(v) => println("Falsche Eingabe"); None
            }
          case _ =>
            println("Falsche Eingabe!"); None
      }
    )

    broadcast.out(0) ~> inputFlowShape ~> merge.in(0)
    FlowShape(broadcast.in, merge.out)
  }

  def analyseInput(input: String): Option[String] =
    val textInputAsList: List[String] = input.split("\\s").toList
    val resultGraph = Source.single(textInputAsList)
      .via(Flow.fromGraph(analyseInputGraph))
      .runWith(Sink.head)
      .map(result => result)
    Await.result(resultGraph, Duration.Inf)

  def validInput(list: List[String]): Try[String] = Try(list.tail.head)

  def writeDown(move: String): Unit = {
    controllerESI.sendPOSTRequest("controller/put", move)
    controllerESI.sendGETRequest("controller/next")
    controllerESI.sendGETRequest("controller/doAndPublish/nextRound")
  }

  private def diceCupPutIn(pi: List[Int]): Unit =
    controllerESI.sendGETRequest(s"controller/doAndPublish/putIn/list=${pi.mkString(",")}")

  private def diceCupPutOut(po: List[Int]): Unit =
    controllerESI.sendGETRequest(s"controller/doAndPublish/putOut/list=${po.mkString(",")}")

  private def checkIfEmpty(index: Int): Boolean =
    (Json.parse(
      fieldESI.sendPOSTRequest(
        s"field/isEmpty/$getPlayerID/$index",
        controllerESI.sendGETRequest("controller/field")
      )
    ) \ "isEmpty").as[Boolean]

  private def getPlayerName: String =
    (Json.parse(gameESI.sendPOSTRequest("game/playerName",
      controllerESI.sendGETRequest("controller/game"))) \ "playerName").as[String]

  private def getPlayerID: Int = {
    (Json.parse(gameESI.sendPOSTRequest("game/playerID",
      controllerESI.sendGETRequest("controller/game"))) \ "playerID").as[Int]
  }

  private def getIndexOfField(posAndDesc: String): Option[Int] =
    Try((Json.parse(diceCupESI.sendGETRequest("diceCup/indexOfField")) \ "indexOfField" \ posAndDesc).as[Int]).toOption

  private def getResult(index: Int): Int =
    (Json.parse(diceCupESI.sendPOSTRequest(s"diceCup/result/$index",
      controllerESI.sendGETRequest("controller/diceCup"))) \ "result").as[Int]

  private def moveToJson(value: Int, x: Int, y: Int): JsObject =
    Json.obj(
      "move" -> Json.obj(
        "value" -> JsNumber(value),
        "x" -> JsNumber(x),
        "y" -> JsNumber(y)
      )
    )

