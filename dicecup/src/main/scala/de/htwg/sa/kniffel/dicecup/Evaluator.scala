package de.htwg.sa.kniffel.dicecup

class Evaluator (strategy: EvaluateStrategy.Type[Int]){
  def result(data: List[Int]): Int = {
    strategy(data)
  }
}
