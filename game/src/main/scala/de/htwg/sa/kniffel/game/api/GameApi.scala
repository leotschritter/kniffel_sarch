package de.htwg.sa.kniffel.game.api

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.scaladsl.GraphDSL.Builder
import de.htwg.sa.kniffel.game.model.IGame
import play.api.libs.json.{JsNull, JsNumber, Json}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class GameApi(using game: IGame):

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val gameFlow: Flow[HttpRequest, String, Any] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*

    val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
    val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

    val getRequestFlow = Flow[HttpRequest].map { req =>
      req.uri.path.toString match {
        case path if path.startsWith("/game/new") =>
          val numberOfPlayers = req.uri.path.toString.split("/").last.toInt
          HttpResponse(entity = game.newGame(numberOfPlayers).toJson.toString)
        case "/game/" =>
          HttpResponse(entity = game.newGame(2).toJson.toString)
        case "/game/ping" =>
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
        case "/game/next" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = game.jsonStringToGame(requestBody).next().match {
              case Some(game) => game.toJson.toString
              case None => Json.obj("game" -> JsNull).toString
            })
          }
        case path if path.startsWith("/game/undoMove") =>
          val segments: Array[String] = req.uri.path.toString().split("/game/undoMove/").last.split("/")
          val value: Int = segments(0).toInt
          val y: Int = segments(1).toInt
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = game.jsonStringToGame(requestBody).undoMove(value, y).toJson.toString)
          }
        case path if path.startsWith("/game/sum") =>
          val segments: Array[String] = req.uri.path.toString().split("/game/sum/").last.split("/")
          val value: Int = segments(0).toInt
          val y: Int = segments(1).toInt
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = game.jsonStringToGame(requestBody).sum(value, y).toJson.toString)
          }
        case "/game/playerID" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("playerID" -> JsNumber(game.jsonStringToGame(requestBody).playerID)).toString)
          }
        case "/game/playerName" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("playerName" -> game.jsonStringToGame(requestBody).playerName).toString)
          }
        case path if path.startsWith("/game/playerName") =>
          val x = req.uri.path.toString.split("/").last.toInt
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("playerName" -> game.jsonStringToGame(requestBody).playerName(x)).toString)
          }
        case "/game/nestedList" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("nestedList" -> game.jsonStringToGame(requestBody).nestedList.map(_.mkString(",")).mkString(";")).toString)
          }
        case "/game/remainingMoves" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("remainingMoves" -> JsNumber(game.jsonStringToGame(requestBody).remainingMoves)).toString)
          }
        case "/game/players" =>
          req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
            val requestBody = entity.data.utf8String
            HttpResponse(entity = Json.obj("players" -> Json.toJson(
              game.jsonStringToGame(requestBody).playerTuples.map { case (id, name) =>
                Json.obj("id" -> JsNumber(id), "name" -> name)
              }
            )).toString)
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

  Http().newServerAt("localhost", 9003).bind(
    pathPrefix("game") {
      extractRequest { request =>
        complete(
          Source.single(request).via(gameFlow).runWith(Sink.head).map(resp => resp)
        )
      }
    }
  )

  def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)