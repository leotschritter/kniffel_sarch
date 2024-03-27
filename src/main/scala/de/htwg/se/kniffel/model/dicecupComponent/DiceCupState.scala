package de.htwg.se.kniffel
package model.dicecupComponent

import model.dicecupComponent.dicecupBaseImpl.DiceCup

import scala.util.Random

trait DiceCupState {
  def throwDices(diceCup: DiceCup): DiceCup
}
class Start extends DiceCupState {
  override def throwDices(diceCup: DiceCup): DiceCup = diceCup
}
class Running extends DiceCupState {
  override def throwDices(diceCup: DiceCup): DiceCup =
    if(diceCup.remDices >= 0)
      DiceCup(diceCup.locked, List.fill(5 - diceCup.locked.size)(Random.between(1, 7)), diceCup.remDices - 1)
    else
      diceCup.state = new Start
      diceCup
}