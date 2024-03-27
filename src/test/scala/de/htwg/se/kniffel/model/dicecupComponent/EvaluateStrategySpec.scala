package de.htwg.se.kniffel
package model.dicecupComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*

class EvaluateStrategySpec extends AnyWordSpec {
  "EvaluateStrategy" when {
    val list: List[Int] = List(6, 6, 6, 6, 6)
    val list2: List[Int] = List(1, 2, 3, 4, 5)
    val list3: List[Int] = List(1, 1, 1, 2, 2)
    "specific functions are called" should {
      "get threeOfAKind result" in {
        EvaluateStrategy.threeOfAKind(list) should be(30)
        EvaluateStrategy.threeOfAKind(list2) should be(0)
      }
      "get fourOfAKind result" in {
        EvaluateStrategy.fourOfAKind(list) should be(30)
        EvaluateStrategy.fourOfAKind(list2) should be(0)
      }
      "get fullHouse result" in {
        EvaluateStrategy.fullHouse(list3) should be(25)
        EvaluateStrategy.fullHouse(list2) should be(0)
      }
      "get bigStreet result" in {
        EvaluateStrategy.bigStreet(list2) should be(40)
        EvaluateStrategy.bigStreet(list3) should be(0)
      }
      "get smallStreet result" in {
        EvaluateStrategy.smallStreet(list2) should be(30)
        EvaluateStrategy.smallStreet(list) should be(0)
      }
      "get kniffel result" in {
        EvaluateStrategy.kniffel(list) should be(50)
        EvaluateStrategy.kniffel(list2) should be(0)
      }
      "get the sum result" in {
        EvaluateStrategy.sum(list) should be(30)
      }
      "get different sums depending on predicate" in {
        EvaluateStrategy.getSum(list3, EvaluateStrategy.checkBigStreet(list3)) should be(0)
        EvaluateStrategy.getSum(list, EvaluateStrategy.checkBigStreet(list2)) should be(30)
      }
      "check if player has diced a bigStreet" in {
        EvaluateStrategy.checkBigStreet(list) should be(false)
        EvaluateStrategy.checkBigStreet(list2) should be(true)
      }
      "map a list to the same list where the number of a value is the value with the same number" in {
        EvaluateStrategy.mapToFrequency(list3) should be(List(3, 3, 3, 2, 2))
      }
    }
  }
}
