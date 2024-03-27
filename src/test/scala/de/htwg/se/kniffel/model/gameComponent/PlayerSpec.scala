package de.htwg.se.kniffel
package model.gameComponent

import model.gameComponent.gameBaseImpl.Player
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class PlayerSpec extends AnyWordSpec {
  "A player" when {
    "created" should {
      "have an ID and a name" in {
        val p1:Player = Player(1, "Dieter")
        p1.playerID should be (1)
        p1.playerName should be ("Dieter")
      }
    }
  }
}
