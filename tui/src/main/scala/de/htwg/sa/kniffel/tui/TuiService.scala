package de.htwg.sa.kniffel.tui

import de.htwg.sa.kniffel.tui.api.TuiApi
import de.htwg.sa.kniffel.tui.aview.TUI

object TuiService:
  val tui: TUI = TUI()

  given TUI = tui

  def main(args: Array[String]): Unit = TuiApi().tui.run()