package de.htwg.sa.kniffel.dicecup.model

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.GraphDSL.Implicits.*

object EvaluateStrategy {
  private val sum: (List[Int], Boolean) => Int = (data, exp) => if (exp) data.sum else 0
  private val mapToFrequency: List[Int] => List[Int] = data => data.map(x => data.count(_ == x))
  private val checkBigStreet: List[Int] => Boolean = data => mapToFrequency(data).max == 1 & data.max - data.min == 4

  val sumFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map((data: List[Int]) => data.sum)
  val threeOfAKindFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map((data: List[Int]) => sum(data, mapToFrequency(data).max >= 3))
  val fourOfAKindFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map((data: List[Int]) => sum(data, mapToFrequency(data).max >= 4))
  val fullHouseFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map((data: List[Int]) => if mapToFrequency(data).max == 3 & mapToFrequency(data).min == 2 then 25 else 0)
  val bigStreetFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map((data: List[Int]) => if mapToFrequency(data).max == 1 & data.max - data.min == 4 then 40 else 0)
  val smallStreetFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map((data: List[Int]) => if checkBigStreet(data) | data.distinct.size == 4 & data.distinct.max - data.distinct.min == 3 | data.distinct.sum.equals(19) | data.distinct.sum.equals(16) then 30 else 0)
  val kniffelFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map((data: List[Int]) => if mapToFrequency(data).max == 5 then 50 else 0)
  val zeroFlow: Flow[List[Int], Int, NotUsed] = Flow[List[Int]].map(_ => 0)
}
