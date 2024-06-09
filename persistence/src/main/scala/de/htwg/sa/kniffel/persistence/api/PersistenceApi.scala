package de.htwg.sa.kniffel.persistence.api

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}
import de.htwg.sa.kniffel.persistence.persistence.IPersistence

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PersistenceApi(using persistence: IPersistence):
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val persistenceFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*

    val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
    val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

    val getRequestFlow = Flow[HttpRequest].map { req =>
      req.uri.path.toString match {
        case "/io/ping" =>
          HttpResponse(entity = "pong")
        case "/io/loadField" =>
          HttpResponse(entity = persistence.loadField)
        case "/io/loadGame" =>
          HttpResponse(entity = persistence.loadGame)
        case "/io/loadDiceCup" =>
          HttpResponse(entity = persistence.loadDiceCup)
        case path if path.startsWith("/io/loadDiceCup/") =>
          val gameId = path.split("/").last.toInt
          HttpResponse(entity = persistence.loadDiceCup(gameId))
        case path if path.startsWith("/io/loadField/") =>
          val gameId = path.split("/").last.toInt
          HttpResponse(entity = persistence.loadField(gameId))
        case path if path.startsWith("/io/loadGame/") =>
          val gameId = path.split("/").last.toInt
          HttpResponse(entity = persistence.loadGame(gameId))
        case path if path.startsWith("/io/createGame/") =>
          val numberOfPlayers = path.split("/").last.toInt
          HttpResponse(entity = persistence.createGame(numberOfPlayers))
        case "/io/loadOptions" =>
          HttpResponse(entity = persistence.loadOptions)
      }
    }
    val getRequestFlowShape = builder.add(getRequestFlow)

    val getResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
      Unmarshal(response.entity).to[String]
    }
    val getResponseFlowShape = builder.add(getResponseFlow)

    val postRequestFlow = Flow[HttpRequest].mapAsync(1) { req =>
      req.uri.path.toString match {
        case "/io/saveField" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = persistence.saveField(requestBody))
          }
        case "/io/saveGame" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = persistence.saveGame(requestBody))
          }
        case "/io/saveDiceCup" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = persistence.saveDiceCup(requestBody))
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

  Http().newServerAt("localhost", 9000).bind(
    pathPrefix("io") {
      extractRequest { request =>
        complete(
          Source.single(request).via(persistenceFlow).runWith(Sink.head).map(resp => resp)
        )
      }
    }
  )

  def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)