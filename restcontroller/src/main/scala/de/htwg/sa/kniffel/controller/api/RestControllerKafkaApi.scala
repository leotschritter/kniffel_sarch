package de.htwg.sa.kniffel.controller.api

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Flow, GraphDSL, Source}
import de.htwg.sa.kniffel.controller.model.IController
import de.htwg.sa.kniffel.controller.util.Move
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.util.Try

class RestControllerKafkaApi(using controller: IController):

  private val tuiTopic = "tui-topic"
  private val tuiResponseTopic = "tui-response-topic"

  implicit val system: ActorSystem[?] = ActorSystem(Behaviors.empty, "kniffel")
  implicit val executionContext: ExecutionContext = system.executionContext

  private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("kniffel-group")

  private val source = Consumer.plainSource(consumerSettings, Subscriptions.topics(tuiTopic))

  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  private val kafkaFlow = Flow[ConsumerRecord[String, String]].map { record =>
    val input = Json.parse(record.value())
    (input \ "msg").as[String]
      .match {
        case "putOut" =>
          controller.doAndPublish(controller.putOut((input \ "list").as[List[Int]]))
          Json.obj("msg" -> "move")
        case "putIn" =>
          controller.doAndPublish(controller.putIn((input \ "list").as[List[Int]]))
          Json.obj("msg" -> "move")
        case "dice" => controller.doAndPublish(controller.dice())
          Json.obj("msg" -> "move")
        case "undo" => controller.undo()
          Json.obj("msg" -> "move")
        case "redo" => controller.redo()
          Json.obj("msg" -> "move")
        case "save" => controller.save()
          Json.obj("msg" -> "save")
        case "loadOptions" => controller.loadOptions
          Json.obj("msg" -> "loadOptions")
        case "load" => Try((input \ "id").as[Int]).toOption match {
          case Some(id) => controller.load(id)
            Json.obj("msg" -> "load")
          case None => controller.load()
            Json.obj("msg" -> "load")
        }
        case "quit" => controller.quit()
          Json.obj("msg" -> "quit")
        case "wd" =>
          val move = input \ "move"
          controller.writeDown(Move(
            (move \ "result").as[Int],
            (move \ "x").as[Int],
            (move \ "y").as[Int]))
          Json.obj("msg" -> "move")
      }
  }

  private val sink = Producer.plainSink(producerSettings)

  def start(): Unit = {
    source
      .via(kafkaFlow)
      .map(result => ProducerRecord[String, String](tuiResponseTopic, Json.stringify(result)))
      .runWith(sink)
  }