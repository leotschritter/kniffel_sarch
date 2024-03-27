package de.htwg.se.kniffel
package model.dicecupComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*

class EvaluatorSpec extends AnyWordSpec {
  val kniffelEvaluator = new Evaluator(EvaluateStrategy.kniffel)
  val threeOfAKindEvaluator = new Evaluator(EvaluateStrategy.threeOfAKind)
  val fourOfAKindEvaluator = new Evaluator(EvaluateStrategy.fourOfAKind)
  val fullHouseEvaluator = new Evaluator(EvaluateStrategy.fullHouse)
  val bigStreetEvaluator = new Evaluator(EvaluateStrategy.bigStreet)
  val smallStreetEvaluator = new Evaluator(EvaluateStrategy.smallStreet)
  val sumEvaluator = new Evaluator(EvaluateStrategy.sum)
  "An Evaluator" when {
    "created" should {
      val list: List[Int] = List(1, 2, 3, 4, 5)
      "have the belonging Evaluator" in {
        kniffelEvaluator.getResult(list) should be(new Evaluator(EvaluateStrategy.kniffel).getResult(list))
        threeOfAKindEvaluator.getResult(list) should be(new Evaluator(EvaluateStrategy.threeOfAKind).getResult(list))
        fourOfAKindEvaluator.getResult(list) should be(new Evaluator(EvaluateStrategy.fourOfAKind).getResult(list))
        fullHouseEvaluator.getResult(list) should be(new Evaluator(EvaluateStrategy.fullHouse).getResult(list))
        bigStreetEvaluator.getResult(list) should be(new Evaluator(EvaluateStrategy.bigStreet).getResult(list))
        smallStreetEvaluator.getResult(list) should be(new Evaluator(EvaluateStrategy.smallStreet).getResult(list))
        sumEvaluator.getResult(list) should be(new Evaluator(EvaluateStrategy.sum).getResult(list))
      }
    }
  }
}
