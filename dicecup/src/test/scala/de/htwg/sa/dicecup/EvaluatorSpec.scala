package de.htwg.sa.dicecup

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class EvaluatorSpec extends AnyWordSpec {
  val kniffelEvaluator = new Evaluator(EvaluateStrategy.kniffel)
  val threeOfAKindEvaluator = new Evaluator(EvaluateStrategy.threeOfAKind)
  val fourOfAKindEvaluator = new Evaluator(EvaluateStrategy.fourOfAKind)
  val fullHouseEvaluator = new Evaluator(EvaluateStrategy.fullHouse)
  val bigStreetEvaluator = new Evaluator(EvaluateStrategy.bigStreet)
  val smallStreetEvaluator = new Evaluator(EvaluateStrategy.smallStreet)
  val sumEvaluator = new Evaluator(EvaluateStrategy.sumChance)
  "An de.htwg.sa.dicecup.Evaluator" when {
    "created" should {
      val list: List[Int] = List(1, 2, 3, 4, 5)
      "have the belonging de.htwg.sa.dicecup.Evaluator" in {
        kniffelEvaluator.result(list) should be(new Evaluator(EvaluateStrategy.kniffel).result(list))
        threeOfAKindEvaluator.result(list) should be(new Evaluator(EvaluateStrategy.threeOfAKind).result(list))
        fourOfAKindEvaluator.result(list) should be(new Evaluator(EvaluateStrategy.fourOfAKind).result(list))
        fullHouseEvaluator.result(list) should be(new Evaluator(EvaluateStrategy.fullHouse).result(list))
        bigStreetEvaluator.result(list) should be(new Evaluator(EvaluateStrategy.bigStreet).result(list))
        smallStreetEvaluator.result(list) should be(new Evaluator(EvaluateStrategy.smallStreet).result(list))
        sumEvaluator.result(list) should be(new Evaluator(EvaluateStrategy.sumChance).result(list))
      }
    }
  }
}
