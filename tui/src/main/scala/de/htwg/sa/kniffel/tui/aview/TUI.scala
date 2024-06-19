package de.htwg.sa.kniffel.tui.aview

import akka.NotUsed
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Flow, Source}
import de.htwg.sa.kniffel.tui.integration.controller.ControllerESI
import de.htwg.sa.kniffel.tui.integration.dicecup.DiceCupESI
import de.htwg.sa.kniffel.tui.integration.field.FieldESI
import de.htwg.sa.kniffel.tui.integration.game.GameESI
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import play.api.libs.json.{JsNumber, JsObject, Json}

import scala.concurrent.ExecutionContext
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class TUI(val gameESI: GameESI, val diceCupESI: DiceCupESI, val fieldESI: FieldESI, val controllerESI: ControllerESI):
  def this() = this(GameESI(), DiceCupESI(), FieldESI(), ControllerESI())

  implicit val system: ActorSystem[?] = ActorSystem(Behaviors.empty, "kniffel")
  implicit val executionContext: ExecutionContext = system.executionContext

  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  private val sink = Producer.plainSink(producerSettings)

  private val kafkaFlow = Flow[List[String]].map { input =>
    input.head match {
      case "q" => Json.obj("msg" -> "quit")
      case "po" =>
        Json.obj("msg" -> "putOut")
          .deepMerge(Json.obj("list" -> input.tail.map(_.toInt)))
      case "pi" =>
        Json.obj("msg" -> "putIn")
          .deepMerge(Json.obj("list" -> input.tail.map(_.toInt)))
      case "d" => Json.obj("msg" -> "dice")
      case "u" => Json.obj("msg" -> "undo")
      case "r" => Json.obj("msg" -> "redo")
      case "s" => Json.obj("msg" -> "save")
      case "lo" => Json.obj("msg" -> "loadOptions")
      case "l" =>
        validInput(input) match {
          case Success(f) =>
            Json.obj("msg" -> "load")
              .deepMerge(Json.obj("id" -> Try(input.tail.head.toInt).toOption.getOrElse(1)))
          case Failure(v) =>
            Json.obj("msg" -> "load")
              .deepMerge(Json.obj("id" -> 1))
        }
      case "wd" =>
        validInput(input) match {
          case Success(f) =>
            val posAndDesc = input.tail.head
            getIndexOfField(posAndDesc)
              .match {
                case Some(index) =>
                  if (checkIfEmpty(index))
                    Json.obj("msg" -> "wd")
                      .deepMerge(moveToJson(getResult(index), getPlayerID, index))
                  else
                    println("Da steht schon was!")
                    Json.obj("msg" -> "empty")
                case None => Json.obj("msg" -> "empty")
              }
          case Failure(v) => Json.obj("msg" -> "empty")
        }
    }
  }
  var continue = true

  private val consumerSettings =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("kniffel-group")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  private val source = Consumer.plainSource(consumerSettings, Subscriptions.topics("tui-response-topic"))

  source.runForeach { record =>
    (Json.parse(record.value()) \ "msg").as[String] match {
      case "quit" => update("quit")
      case "save" => update("save")
      case "empty" => update("empty")
      case _ => update("move")
    }
  }

  def run(): Unit =
    println(fieldESI.sendPOSTRequest("field/mesh", controllerESI.sendGETRequest("controller/field")))
    inputLoop()

  def update(event: String): String =
    event match {
      case "quit" => continue = false; Json.obj("event" -> event).toString
      case "save" => continue; Json.obj("event" -> event).toString
      case "empty" => Json.obj("event" -> event).toString
      case _ => println(controllerESI.sendGETRequest("controller/")); Json.obj("event" -> event).toString
    }


  private def inputLoop(): Unit =
    while continue do analyseInput(readLine)

  private def analyseInput(input: String): Unit =
    Source.single(input.split(" ").toList)
      .via(kafkaFlow)
      .map(result =>
        new ProducerRecord[String, String]("tui-topic", Json.stringify(result)))
      .runWith(sink)

  private def validInput(list: List[String]): Try[String] = Try(list.tail.head)

  def writeDown(move: String): Unit = {
    controllerESI.sendPOSTRequest("controller/put", move)
    controllerESI.sendGETRequest("controller/next")
    controllerESI.sendGETRequest("controller/doAndPublish/nextRound")
  }
  
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

  private def getPlayerID: Int =
    (Json.parse(gameESI.sendPOSTRequest("game/playerID",
      controllerESI.sendGETRequest("controller/game"))) \ "playerID").as[Int]

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

