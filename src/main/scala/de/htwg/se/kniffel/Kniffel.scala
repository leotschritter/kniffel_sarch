package de.htwg.se.kniffel

import aview.{GUI, TUI}
import Config.given

@main def main(): Unit =
  println("Welcome to Kniffel")
  val tui = TUI()
  val gui = new GUI()
  tui.run()

