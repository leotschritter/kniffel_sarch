package de.htwg.sa.kniffel.gui

import de.htwg.sa.kniffel.gui.api.GuiApi
import de.htwg.sa.kniffel.gui.aview.GUI

object GUIService:
  val gui: GUI = GUI()
  given GUI = gui

  def main(args: Array[String]): Unit = GuiApi().start
  