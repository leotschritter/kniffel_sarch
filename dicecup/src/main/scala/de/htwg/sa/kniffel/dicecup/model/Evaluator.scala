package de.htwg.sa.kniffel.dicecup.model

class Evaluator (strategy: EvaluateStrategy.Type[Int]){
  def result(data: List[Int]): Int = {
    strategy(data)
  }
}
