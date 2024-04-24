package de.htwg.sa.kniffel.dicecup.model

object EvaluateStrategy {
  type Type[Int] = List[Int] => Int

  val sum: (List[Int], Boolean) => Int = (data, exp) => if (exp) data.sum else 0

  val mapToFrequency: List[Int] => List[Int] = data => data.map(x => data.count(_ == x))

  val checkBigStreet: List[Int] => Boolean = data => mapToFrequency(data).max == 1 & data.max - data.min == 4

  val threeOfAKind: List[Int] => Int = data => sum(data, mapToFrequency(data).max >= 3)

  val fourOfAKind: List[Int] => Int = data => sum(data, mapToFrequency(data).max >= 4)

  val fullHouse: List[Int] => Int = data => if mapToFrequency(data).max == 3 & mapToFrequency(data).min == 2 then 25 else 0

  val bigStreet: List[Int] => Int = data => if mapToFrequency(data).max == 1 & data.max - data.min == 4 then 40 else 0

  val smallStreet: List[Int] => Int = data => if checkBigStreet(data) | data.distinct.size == 4 & data.distinct.max - data.distinct.min == 3 | data.distinct.sum.equals(19) | data.distinct.sum.equals(16) then 30 else 0

  val kniffel: List[Int] => Int = data => if mapToFrequency(data).max == 5 then 50 else 0

  val sumChance: List[Int] => Int = data => data.sum

}
