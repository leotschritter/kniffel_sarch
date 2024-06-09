package de.htwg.sa.kniffel.field.api

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}
import de.htwg.sa.kniffel.field.model.IField
import play.api.libs.json.{JsBoolean, JsNull, JsNumber, Json}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class FieldApi(using field: IField):

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val fieldFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*

    val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
    val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

    val getRequestFlow = Flow[HttpRequest].map { req =>
      req.uri.path.toString match {
        case path if path.startsWith("/field/new") =>
          val numberOfPlayers = req.uri.path.toString.split("/").last.toInt
          HttpResponse(entity = field.newField(numberOfPlayers).toJson.toString)
        case "/field" =>
          HttpResponse(entity = field.newField(2).toJson.toString)
        case "/field/ping" =>
          HttpResponse(entity = "pong")
      }
    }
    val getRequestFlowShape = builder.add(getRequestFlow)

    val getResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
      Unmarshal(response.entity).to[String]
    }
    val getResponseFlowShape = builder.add(getResponseFlow)

    val postRequestFlow = Flow[HttpRequest].mapAsync(1) { req =>
      req.uri.path.toString match {
        case path if path.startsWith("/field/putMulti") =>
          val segments: Array[String] = req.uri.path.toString().split("/field/putMulti/").last.split("/")
          val valueList: List[Int] = segments(0).split("list=").last.split(",").map(_.toInt).toList
          val putInValue: Int = segments(1).toInt
          val x: Int = segments(2).toInt
          val y: Int = segments(3).toInt
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = field.jsonStringToField(requestBody).putMulti(valueList, Some(putInValue), x, y).toJson.toString)
          }
        case path if path.startsWith("/field/undoMove") =>
          val segments: Array[String] = req.uri.path.toString().split("/field/undoMove/").last.split("/")
          val valueList: List[Int] = segments(0).split("list=").last.split(",").map(_.toInt).toList
          val x: Int = segments(1).toInt
          val y: Int = segments(2).toInt
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = field.jsonStringToField(requestBody).undoMove(valueList, x, y).toJson.toString)
          }
        case path if path.startsWith("/field/cell") =>
          val segments: Array[String] = req.uri.path.toString().split("/field/cell/").last.split("/")
          val col: Int = segments(0).toInt
          val row: Int = segments(1).toInt
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = field.jsonStringToField(requestBody).matrix.cell(col, row).match {
              case Some(value) => Json.obj("value" -> JsNumber(value)).toString
              case None => Json.obj("value" -> JsNull).toString
            })
          }
        case "/field/numberOfPlayers" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("numberOfPlayers" -> JsNumber(field.jsonStringToField(requestBody).numberOfPlayers)).toString)
          }
        case path if path.startsWith("/field/isEmpty") =>
          val segments: Array[String] = req.uri.path.toString().split("/field/isEmpty/").last.split("/")
          val col: Int = segments(0).toInt
          val row: Int = segments(1).toInt
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("isEmpty" -> JsBoolean(field.jsonStringToField(requestBody).matrix.isEmpty(col, row))).toString)
          }
        case "/field/mesh" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = field.jsonStringToField(requestBody).toString)
          }
        case _ =>
          Future.successful(HttpResponse(404, entity = "Unknown route"))
      }
    }
    val postRequestFlowShape = builder.add(postRequestFlow)

    val postResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
      Unmarshal(response.entity).to[String]
    }
    val postResponseFlowShape = builder.add(postResponseFlow)

    broadcast.out(0) ~> getRequestFlowShape ~> getResponseFlowShape ~> merge.in(0)
    broadcast.out(1) ~> postRequestFlowShape ~> postResponseFlowShape ~> merge.in(1)

    FlowShape(broadcast.in, merge.out)
  })

  Http().newServerAt("localhost", 9001).bind(
    pathPrefix("field") {
      extractRequest { request =>
        complete(
          Source.single(request).via(fieldFlow).runWith(Sink.head).map(resp => resp)
        )
      }
    }
  )

  def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)