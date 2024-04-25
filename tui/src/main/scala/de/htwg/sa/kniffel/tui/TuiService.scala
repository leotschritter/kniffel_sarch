package de.htwg.sa.kniffel.tui

import de.htwg.sa.kniffel.tui.api.TuiApi
import de.htwg.sa.kniffel.tui.aview.TUI

object TuiService {

  def main(args: Array[String]): Unit = {
    val tuiApi: TuiApi = new TuiApi(new TUI())
    tuiApi.tui.run()
  }

}
