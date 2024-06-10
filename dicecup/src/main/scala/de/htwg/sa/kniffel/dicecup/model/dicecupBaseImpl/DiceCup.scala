package de.htwg.sa.kniffel.dicecup.model.dicecupBaseImpl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, Graph}
import de.htwg.sa.kniffel.dicecup.model.EvaluateStrategy.*
import de.htwg.sa.kniffel.dicecup.model.{EvaluateStrategy, IDiceCup}
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}

import scala.collection.immutable.ListMap
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Random

case class DiceCup(locked: List[Int], inCup: List[Int], remDices: Int) extends IDiceCup:
  def this() = this(List.fill(0)(0), List.fill(5)(Random.between(1, 7)), 2)

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = system.dispatcher

  def dice(): Option[DiceCup] =
    if (remDices >= 0)
      Some(DiceCup(locked, List.fill(5 - locked.size)(Random.between(1, 7)), remDices - 1))
    else
      None

  def remainingDices: Int = remDices

  def dropListEntriesFromList(entriesToDelete: List[Int])(shortenedList: List[Int])(n: Int = 0): List[Int] = {
    if (entriesToDelete.size != n)
      dropListEntriesFromList(entriesToDelete)(shortenedList.take(shortenedList.lastIndexOf(entriesToDelete.apply(n)))
        ++ shortenedList.drop(shortenedList.lastIndexOf(entriesToDelete.apply(n)) + 1))(n + 1)
    else
      shortenedList
  }

  def nextRound(): DiceCup = DiceCup(List.fill(0)(0), List.fill(0)(0), 2)

  private def subsetOf(inOrOut: List[Int])(existingList: List[Int]): Boolean = inOrOut.forall(existingList.contains(_))

  def putDicesIn(sortIn: List[Int]): DiceCup = {
    if (subsetOf(sortIn)(locked))
      DiceCup(dropListEntriesFromList(sortIn)(locked)(), inCup ++ sortIn, remDices)
    else
      this
  }

  def putDicesOut(sortOut: List[Int]): DiceCup = {
    if (subsetOf(sortOut)(inCup))
      DiceCup(sortOut ++ locked, dropListEntriesFromList(sortOut)(inCup)(), remDices)
    else
      this
  }

  private def mergeLists(list1: List[Int])(list2: List[Int]): List[Int] = list1 ::: list2

  def evaluate(index: Int): Graph[FlowShape[List[Int], Int], NotUsed] = GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    import GraphDSL.Implicits.*
    
    val broadcast = builder.add(Broadcast[List[Int]](1))
    val merge = builder.add(Merge[Int](1))
    
    val selectFlowShape = builder.add(
      index match {
        case 0 | 1 | 2 | 3 | 4 | 5 => sumFlow
        case 9 => threeOfAKindFlow
        case 10 => fourOfAKindFlow
        case 11 => fullHouseFlow
        case 12 => smallStreetFlow
        case 13 => bigStreetFlow
        case 14 => kniffelFlow
        case 15 => sumFlow
        case _ => zeroFlow
      }
    )
    broadcast.out(0) ~> selectFlowShape ~> merge.in(0)
    FlowShape(broadcast.in, merge.out)
  }

  def result(index: Int): Int =
    val list: List[Int] = mergeLists(inCup)(locked)
    if (list.isEmpty)
      return 0

    val resultGraph = Source.single(if index < 6 && index >= 0 then list.filter(_ == index + 1) else list)
      .via(Flow.fromGraph(evaluate(index)))
      .runWith(Sink.head)
      .map(result => result)
    Await.result(resultGraph, Duration.Inf)

  def indexOfField: ListMap[String, Int] =
    ListMap("1" -> 0, "2" -> 1, "3" -> 2, "4" -> 3, "5" -> 4, "6" -> 5,
      "3X" -> 9, "4X" -> 10, "FH" -> 11, "KS" -> 12, "GS" -> 13, "KN" -> 14, "CH" -> 15)

  override def toString: String = "Im Becher: " + inCup.mkString(" ")
    + "\nRausgenommen: " + locked.mkString(" ")
    + "\nVerbleibende Würfe: " + (remDices + 1)
    + "\nBitte wählen Sie aus: " + indexOfField.keys.mkString(" ")
    + "\n"

  override def toJson: JsObject = {
    Json.obj(
      "dicecup" -> Json.obj(
        "stored" -> this.locked,
        "incup" -> this.inCup,
        "remainingDices" -> JsNumber(this.remainingDices)
      )
    )
  }



  override def jsonStringToDiceCup(diceCup: String): IDiceCup = {
    val json: JsValue = Json.parse(diceCup)
    val diceCupJson: JsValue = (json \ "dicecup").get
    val storedList: List[Int] = (diceCupJson \ "stored").get.as[List[Int]]
    val inCupList: List[Int] = (diceCupJson \ "incup").get.as[List[Int]]
    val remDice: Int = (diceCupJson \ "remainingDices").get.toString.toInt

    DiceCup(storedList, inCupList, remDice)
  }