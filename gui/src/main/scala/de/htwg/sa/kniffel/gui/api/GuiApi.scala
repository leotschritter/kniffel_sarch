package de.htwg.sa.kniffel.gui.api

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}
import de.htwg.sa.kniffel.gui.aview.GUI

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class GuiApi(using gui: GUI):
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val guiFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*

    val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
    val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

    val getRequestFlow = Flow[HttpRequest].map { req =>
      req.uri.path.toString match {
        case "/gui/ping" =>
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
        case "/gui/quit" =>
          Future.successful(HttpResponse(entity = gui.update("quit")))
        case "/gui/save" =>
          Future.successful(HttpResponse(entity = gui.update("save")))
        case "/gui/load" =>
          Future.successful(HttpResponse(entity = gui.update("load")))
        case "/gui/move" =>
          Future.successful(HttpResponse(entity = gui.update("move")))
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

  Http().newServerAt("localhost", 9004).bind(
    pathPrefix("gui") {
      extractRequest { request =>
        complete(
          Source.single(request).via(guiFlow).runWith(Sink.head).map(resp => resp)
        )
      }
    }
  )

  def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)