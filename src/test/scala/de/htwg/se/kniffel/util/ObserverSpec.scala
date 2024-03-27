package de.htwg.se.kniffel
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import util.Event

class ObserverSpec extends AnyWordSpec {
  "An observable" when {
    "new observer" should {
      var updated = false
      val observable = new Observable
      val observer = new Observer {
        override def update(e: Event): Unit = updated = true
      }

      observable.add(observer)

      "have a subscriber" in {
        observable.subscribers.contains(observer) should be(true)
      }
      "have subscriber removed" in {
        observable.remove(observer)
        observable.subscribers.contains(observer) should be(false)
      }
      "have a subscriber add" in {
        observable.add(observer)
        observable.subscribers.contains(observer) should be(true)
      }
      "remove an Observer" in {
        observable.remove(observer)
        observable.subscribers should not contain (observer)
      }
      "add an Observer" in {
        observable.add(observer)
        observable.subscribers should contain (observer)
      }

    }
  }
}
