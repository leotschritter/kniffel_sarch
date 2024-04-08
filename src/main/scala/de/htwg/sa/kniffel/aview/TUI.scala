package de.htwg.sa.kniffel
package aview

import de.htwg.sa.kniffel.controller.IController
import de.htwg.sa.kniffel.model.Move
import de.htwg.sa.kniffel.util.{Event, Observer}

import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class TUI(using controller: IController) extends UI(controller) with Observer:
  controller.add(this)
  var continue = true

  override def run(): Unit =
    println(controller.field.toString)
    inputLoop()

  def update(e: Event): Unit =
    e match {
      case Event.Quit => continue = false
      case Event.Save => continue
      case _ => println(controller.field.toString + "\n" + controller.diceCup.toString + controller.game.playerName + " ist an der Reihe.")
    }


  def inputLoop(): Unit =
    analyseInput(readLine) match
      case None => inputLoop()
      case Some(move) => writeDown(move)
    if continue then inputLoop()


  def analyseInput(input: String): Option[Move] =
    val textInputAsList = input.split("\\s").toList
    textInputAsList.head match
      case "q" => None
      case "po" => diceCupPutOut(textInputAsList.tail.map(_.toInt)); None
      case "pi" => diceCupPutIn(textInputAsList.tail.map(_.toInt)); None
      case "d" => controller.doAndPublish(controller.dice()); None
      case "u" => controller.undo(); None
      case "r" => controller.redo(); None
      case "s" => controller.save(); None
      case "l" => controller.load(); None
      case "wd" =>
        validInput(textInputAsList) match {
          case Success(f) => val posAndDesc = textInputAsList.tail.head
            controller.diceCup.indexOfField.get(posAndDesc)
              .match {
                case Some(index) =>
                  if (controller.field.matrix.isEmpty(controller.game.playerID, index))
                    Some(Move(controller.diceCup.result(index), controller.game.playerID, index))
                  else
                    println("Da steht schon was!")
                    None
                case None => println("Falsche Eingabe!"); None
              }
          case Failure(v) => println("Falsche Eingabe"); None
        }
      case _ =>
        println("Falsche Eingabe!"); None

  def validInput(list: List[String]): Try[String] = Try(list.tail.head)

  def getController: IController = controller
         