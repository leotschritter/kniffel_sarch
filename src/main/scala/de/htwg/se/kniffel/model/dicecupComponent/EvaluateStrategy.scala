package de.htwg.se.kniffel
package model.dicecupComponent

object EvaluateStrategy {
  type Type[Int] = List[Int] => Int

  def getSum(data: List[Int], exp: Boolean): Int = if (exp) data.sum else 0

  def mapToFrequency(data: List[Int]): List[Int] = data.map(x => data.count(_ == x))

  def checkBigStreet(data: List[Int]) : Boolean = mapToFrequency(data).max == 1 & data.max - data.min == 4

  def threeOfAKind(data: List[Int]): Int = getSum(data, mapToFrequency(data).max >= 3)

  def fourOfAKind(data: List[Int]): Int = getSum(data, mapToFrequency(data).max >= 4)

  def fullHouse(data: List[Int]): Int = if mapToFrequency(data).max == 3 & mapToFrequency(data).min == 2 then 25 else 0

  def bigStreet(data: List[Int]): Int = if mapToFrequency(data).max == 1 & data.max - data.min == 4 then 40 else 0

  def smallStreet(data: List[Int]): Int = if checkBigStreet(data) | data.distinct.size == 4 & data.distinct.max - data.distinct.min == 3 | data.distinct.sum.equals(19) | data.distinct.sum.equals(16) then 30 else 0

  def kniffel(data: List[Int]): Int = if mapToFrequency(data).max == 5 then 50 else 0

  def sum(data: List[Int]): Int = data.sum

}
