package de.htwg.sa.kniffel.controller.entity

import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.be
import org.scalatest.wordspec.AnyWordSpec

class DiceCupTest extends AnyWordSpec with Matchers:

  "Dice Cup" when {
    "created" should {
      "have 5 Dices" in {
        val diceCup: DiceCup = new DiceCup()
        diceCup.inCup.size should be(5)
        diceCup.locked.size should be(0)
        diceCup.inCup.foreach {
          s =>
            s should be < 7
            s should be > 0
        }
  
      }
    }
    "dices are thrown" should {
      val diceCup: DiceCup = new DiceCup()
    }
    "list Entries are dropped from another list" should {
      val diceCup: DiceCup = new DiceCup()
      val list: List[Int] = List.range(1, 6)
    }
    "when converted to JSON" should {
      "look like" in {
        val diceCup: DiceCup = DiceCup(List(2, 2), List(2, 2, 2), 2)
        diceCup.toJson.toString should be(
          "{\"dicecup\":{\"stored\":[2,2],\"incup\":[2,2,2],\"remainingDices\":2}}")
      }
    }
    "when converted from JSON" should {
      "look like" in {
        val diceCup: DiceCup = DiceCup(List(2, 2), List(2, 2, 2), 2)
        diceCup.jsonStringToDiceCup("{\"dicecup\":{\"stored\":[2,2],\"incup\":[2,2,2],\"remainingDices\":2}}") should be(
          diceCup)
      }
    }
  
  
    /*"when DiceCupState changed" should {
      val diceCup: DiceCup = new DiceCup()
      diceCup.state = new Start
      "have the Start State" in {
        diceCup.dice()
        diceCup.inCup.size + diceCup.locked.size should be(5)
        diceCup.inCup.foreach {
          s =>
            s should be < 7
            s should be > 0
        }
      }
      "have the Running State" in {
        var diceCup1 = diceCup.dice()
        diceCup1 = diceCup1.dice()
        diceCup1 = diceCup1.dice()
        val diceCup2 = diceCup1.dice()
        diceCup1.dice().toString() should be (diceCup2.toString())
      }
    }*/
  }
  
  