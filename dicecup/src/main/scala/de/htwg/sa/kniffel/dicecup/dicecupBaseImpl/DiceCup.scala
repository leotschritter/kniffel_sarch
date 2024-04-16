package de.htwg.sa.kniffel.dicecup.dicecupBaseImpl

import de.htwg.sa.kniffel.dicecup.{EvaluateStrategy, Evaluator, IDiceCup}

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
