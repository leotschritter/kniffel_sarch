package de.htwg.sa.kniffel.dicecup.api

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, Materializer, UniformFanInShape, UniformFanOutShape}
import de.htwg.sa.kniffel.dicecup.model.IDiceCup
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsNull, JsNumber, Json}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class DiceCupApi(using diceCup: IDiceCup):
  private val log: Logger = LoggerFactory.getLogger(classOf[DiceCupApi])

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val diceCupFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*

    val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
    val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

    val getRequestFlow = Flow[HttpRequest].map { req =>
      req.uri.path.toString match {
        case "/diceCup" =>
          HttpResponse(entity = diceCup.toJson.toString)
        case "/diceCup/ping" =>
          HttpResponse(entity = "pong")
        case "/diceCup/indexOfField" =>
          HttpResponse(entity = Json.obj("indexOfField" -> diceCup.indexOfField).toString)
      }
    }
    val getRequestFlowShape = builder.add(getRequestFlow)

    val getResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
      Unmarshal(response.entity).to[String]
    }
    val getResponseFlowShape = builder.add(getResponseFlow)

    val postRequestFlow = Flow[HttpRequest].mapAsync(1) { req =>
      req.uri.path.toString match {
        case "/diceCup/inCup" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("inCup" -> diceCup.jsonStringToDiceCup(requestBody).inCup).toString)
          }
        case "/diceCup/locked" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("locked" -> diceCup.jsonStringToDiceCup(requestBody).locked).toString)
          }
        case "/diceCup/remainingDices" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("remainingDices" -> JsNumber(diceCup.jsonStringToDiceCup(requestBody).remainingDices)).toString)
          }
        case path if path.startsWith("/diceCup/result")  =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            val index = req.uri.toString().split('/').last.toInt
            HttpResponse(entity = Json.obj("result" -> JsNumber(diceCup.jsonStringToDiceCup(requestBody).result(index))).toString)
          }
        case "/diceCup/nextRound" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = diceCup.jsonStringToDiceCup(requestBody).nextRound().toJson.toString)
          }
        case path if path.startsWith("/diceCup/putOut")  =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            val list: List[Int] = req.uri.toString().split("list=").last.split(',').map(_.toInt).toList
            HttpResponse(entity = diceCup.jsonStringToDiceCup(requestBody).putDicesOut(list).toJson.toString)
          }
        case path if path.startsWith("/diceCup/putIn")  =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            val list: List[Int] = req.uri.toString().split("list=").last.split(',').map(_.toInt).toList
            HttpResponse(entity = diceCup.jsonStringToDiceCup(requestBody).putDicesIn(list).toJson.toString)
          }
        case "/diceCup/dice" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity =
              diceCup.jsonStringToDiceCup(requestBody).dice().match {
                case Some(diceCup) => diceCup.toJson.toString
                case None => Json.obj("dicecup" -> JsNull).toString
              }
            )
          }
        case "/diceCup/representation" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = diceCup.jsonStringToDiceCup(requestBody).toString)
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

  private val route: Route = {
    pathPrefix("diceCup") {
      extractRequest { request =>
        complete (
          Source.single(request).via(diceCupFlow).runWith(Sink.head).map(resp => resp)
        )
      }
    }
  }

  Http().newServerAt("localhost", 9002).bind(route)
  def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)
