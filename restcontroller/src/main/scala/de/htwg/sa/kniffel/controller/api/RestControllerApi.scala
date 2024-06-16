package de.htwg.sa.kniffel.controller.api

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}
import de.htwg.sa.kniffel.controller.integration.gui.GuiESI
import de.htwg.sa.kniffel.controller.integration.tui.TuiESI
import de.htwg.sa.kniffel.controller.model.IController
import de.htwg.sa.kniffel.controller.util.Event.*
import de.htwg.sa.kniffel.controller.util.{Event, Move, Observer}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class RestControllerApi(using controller: IController, tuiESI: TuiESI, guiESI: GuiESI) extends Observer:
  controller.add(this)

  override def update(e: Event): Unit = e match
    case Event.Save => /*guiESI.sendRequest("gui/save");*/ tuiESI.sendRequest("tui/save")
    case Event.Load => /*guiESI.sendRequest("gui/load");*/ tuiESI.sendRequest("tui/load")
    case Event.Quit => /*guiESI.sendRequest("gui/quit");*/ tuiESI.sendRequest("tui/quit")
    case Event.Move => /*guiESI.sendRequest("gui/move");*/ tuiESI.sendRequest("tui/move")
    
    
  

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val controllerFlow: Flow[HttpRequest, String, Any] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*

    val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
    val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

    val getRequestFlow = Flow[HttpRequest].map { req =>
      req.uri.path.toString match {
        case "/controller/ping" =>
          HttpResponse(entity = "pong")
        case "/controller/" =>
          HttpResponse(entity = controller.toString)
        case "/controller/controller" =>
          HttpResponse(entity = controller.toJson.toString)
        case "/controller/field" =>
          HttpResponse(entity = controller.field.toJson.toString)
        case "/controller/game" =>
          HttpResponse(entity = controller.game.toJson.toString)
        case "/controller/diceCup" =>
          HttpResponse(entity = controller.diceCup.toJson.toString)
        case "/controller/load" =>
          HttpResponse(entity = controller.load())
        case "/controller/next" =>
          HttpResponse(entity = controller.next())
        case "/controller/loadOptions" =>
          HttpResponse(entity = controller.loadOptions)
        case path if path.startsWith("/controller/load/") =>
          val id = path.split("/").last.toInt
          HttpResponse(entity = controller.load(id))
        case "/controller/doAndPublish/nextRound" =>
          HttpResponse(entity = controller.doAndPublish(controller.nextRound()))
        case "/controller/doAndPublish/dice" =>
          HttpResponse(entity = controller.doAndPublish(controller.dice()))
        case path if path.startsWith("/controller/doAndPublish/putIn") =>
          val pi = path.stripPrefix("/controller/doAndPublish/putIn/list=").split(",").map(_.toInt).toList
          HttpResponse(entity = controller.doAndPublish(controller.putIn(pi)))
        case path if path.startsWith("/controller/doAndPublish/putOut") =>
          val po = path.stripPrefix("/controller/doAndPublish/putOut/list=").split(",").map(_.toInt).toList
          HttpResponse(entity = controller.doAndPublish(controller.putOut(po)))
        case path if path.startsWith("/controller/writeDown/") =>
          val value = path.stripPrefix("/controller/writeDown/")
          Try {
            val currentPlayer = controller.gameESI.sendPlayerIDRequest(controller.game)
            val indexOfField = controller.diceCupESI.sendIndexOfFieldRequest(value)
            val result = controller.diceCupESI.sendResultRequest(indexOfField, controller.diceCup)
            HttpResponse(entity = controller.writeDown(Move(result, currentPlayer, indexOfField)))
          } match {
            case Failure(_) => HttpResponse(entity = "Invalid Input!")
            case Success(response) => response
          }
        case "/controller/save" =>
          HttpResponse(entity = controller.save())
        case "/controller/undo" =>
          HttpResponse(entity = controller.undo())
        case "/controller/redo" =>
          HttpResponse(entity = controller.redo())
      }
    }
    val getRequestFlowShape = builder.add(getRequestFlow)

    val getResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
      Unmarshal(response.entity).to[String]
    }
    val getResponseFlowShape = builder.add(getResponseFlow)

    val postRequestFlow = Flow[HttpRequest].mapAsync(1) { req =>
      req.uri.path.toString match {
        case "/controller/put" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = controller.put(controller.jsonStringToMove(requestBody)))
          }
        case "/controller/quit" =>
          Future.successful(HttpResponse(entity = controller.quit()))
        case "/controller/nextRound" =>
          Future.successful(HttpResponse(entity = controller.nextRound().toJson.toString))
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

  Http().newServerAt("localhost", 9006).bind(
    concat(
      pathPrefix("controller") {
        extractRequest { request =>
          complete(
            Source.single(request).via(controllerFlow).runWith(Sink.head).map(resp => resp)
          )
        }
      }
    )
  )

  def start: Future[Nothing] = {
    controller.fileIOESI.createGameRequest(2)
    Await.result(Future.never, Duration.Inf)
  }