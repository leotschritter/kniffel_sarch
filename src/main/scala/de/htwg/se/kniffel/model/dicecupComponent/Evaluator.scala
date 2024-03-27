package de.htwg.se.kniffel
package model.dicecupComponent

class Evaluator (strategy: EvaluateStrategy.Type[Int]){
  def getResult(data: List[Int]): Int = {
    strategy(data)
  }
}
