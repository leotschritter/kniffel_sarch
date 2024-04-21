package de.htwg.sa.kniffel.dicecup.dicecupBaseImpl

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import de.htwg.sa.kniffel.dicecup.{EvaluateStrategy, Evaluator, IDiceCup}
import play.api.libs.json.{JsNull, JsNumber, JsObject, JsValue, Json}

import scala.collection.immutable.ListMap
import scala.util.Random

case class DiceCup(locked: List[Int], inCup: List[Int], remDices: Int) extends IDiceCup:
  def this() = this(List.fill(0)(0), List.fill(5)(Random.between(1, 7)), 2)

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

  def result(index: Int): Int =
    val list: List[Int] = mergeLists(inCup)(locked)
    index match {
      case 0 | 1 | 2 | 3 | 4 | 5 => list.filter(_ == index + 1).sum
      case 9 => new Evaluator(EvaluateStrategy.threeOfAKind).result(list)
      case 10 => new Evaluator(EvaluateStrategy.fourOfAKind).result(list)
      case 11 => new Evaluator(EvaluateStrategy.fullHouse).result(list)
      case 12 => new Evaluator(EvaluateStrategy.smallStreet).result(list)
      case 13 => new Evaluator(EvaluateStrategy.bigStreet).result(list)
      case 14 => new Evaluator(EvaluateStrategy.kniffel).result(list)
      case 15 => new Evaluator(EvaluateStrategy.sumChance).result(list)
      case _ => 0
    }

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

  private val IntList: PathMatcher1[List[Int]] = PathMatcher("""list=\d+(?:,\d+)*""".r).flatMap { str =>
    val ints = str.split("=").tail.mkString(",").split(",").map(_.toInt)
    Some(ints.toList)
  }

  override val diceCupRoute: Route =
    concat(
      get {
        concat(
          pathSingleSlash {
            complete(toJson.toString)
          },
          path("indexOfField") {
            complete(Json.obj("indexOfField" -> this.indexOfField).toString)
          },
          path("") {
            sys.error("No such GET route")
          }
        )
      },
      post {
        concat(
          path("inCup") {
            entity(as[String]) { requestBody =>
              complete(Json.obj("inCup" -> jsonStringToDiceCup(requestBody).inCup).toString)
            }
          },
          path("locked") {
            entity(as[String]) { requestBody =>
              complete(Json.obj("locked" -> jsonStringToDiceCup(requestBody).locked).toString)
            }
          },
          path("remainingDices") {
            entity(as[String]) { requestBody =>
              complete(Json.obj("remainingDices" -> JsNumber(jsonStringToDiceCup(requestBody).remainingDices)).toString)
            }
          },
          path("result" / IntNumber) { (index: Int) =>
            entity(as[String]) { requestBody =>
              complete(Json.obj("result" -> JsNumber(jsonStringToDiceCup(requestBody).result(index))).toString)
            }
          },
          path("nextRound") {
            entity(as[String]) { requestBody =>
              complete(jsonStringToDiceCup(requestBody).nextRound().toJson.toString)
            }
          },
          // example: putOut/list=1,2,3
          path("putOut" / IntList) { (list: List[Int]) =>
            entity(as[String]) { requestBody =>
              complete(jsonStringToDiceCup(requestBody).putDicesOut(list).toJson.toString)
            }
          },
          path("putIn" / IntList) { (list: List[Int]) =>
            entity(as[String]) { requestBody =>
              complete(jsonStringToDiceCup(requestBody).putDicesIn(list).toJson.toString)
            }
          },
          path("dice") {
            entity(as[String]) { requestBody =>
              complete(
                jsonStringToDiceCup(requestBody).dice().match {
                  case Some(diceCup) => diceCup.toJson.toString
                  case None => Json.obj("dicecup" -> JsNull).toString
                }
              )
            }
          },
          path("representation") {
            entity(as[String]) { requestBody =>
              complete(jsonStringToDiceCup(requestBody).toString)
            }
          },
          path("") {
            sys.error("No such POST route")
          }
        )
      }
    )

  override def jsonStringToDiceCup(diceCup: String): IDiceCup = {
    val json: JsValue = Json.parse(diceCup)
    val diceCupJson: JsValue = (json \ "dicecup").get
    val storedList: List[Int] = (diceCupJson \ "stored").get.as[List[Int]]
    val inCupList: List[Int] = (diceCupJson \ "incup").get.as[List[Int]]
    val remDice: Int = (diceCupJson \ "remainingDices").get.toString.toInt

    DiceCup(storedList, inCupList, remDice)
  }