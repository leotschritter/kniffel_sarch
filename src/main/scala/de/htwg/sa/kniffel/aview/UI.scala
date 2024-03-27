package de.htwg.sa.kniffel
package aview

import controller.IController
import util.Observer
import model.Move

trait UI(controller: IController) extends Observer {

  def run(): Unit = {}

  def writeDown(move: Move): Unit = {
    controller.put(move)
    controller.next()
    controller.doAndPublish(controller.nextRound())
  }

  def diceCupPutIn(pi: List[Int]): Unit = controller.doAndPublish(controller.putIn, pi)

  def diceCupPutOut(po: List[Int]): Unit = controller.doAndPublish(controller.putOut, po)
}
