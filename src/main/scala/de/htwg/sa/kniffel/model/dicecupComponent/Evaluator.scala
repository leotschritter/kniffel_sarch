package de.htwg.sa.kniffel
package model.dicecupComponent

class Evaluator (strategy: EvaluateStrategy.Type[Int]){
  def result(data: List[Int]): Int = {
    strategy(data)
  }
}
