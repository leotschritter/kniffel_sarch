package de.htwg.sa.kniffel.controller.util

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class MoveSpec extends AnyWordSpec {
  "A Move" when {
    "created" should {
      "have a value and coordinations" in {
        val m1:Move = Move(73, 1, 1)
        m1.value should be (73)
        m1.x & m1.y should be (1)
      }
    }
  }
}
