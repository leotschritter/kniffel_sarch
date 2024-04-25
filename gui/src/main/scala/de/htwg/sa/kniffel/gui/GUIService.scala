package de.htwg.sa.kniffel.gui

import de.htwg.sa.kniffel.gui.api.GuiApi
import de.htwg.sa.kniffel.gui.aview.GUI

object GUIService {

  def main(args: Array[String]): Unit = {
    val guiApi: GuiApi = new GuiApi(new GUI())
  }


}
