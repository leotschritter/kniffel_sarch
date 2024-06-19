package de.htwg.sa.kniffel.tui

import de.htwg.sa.kniffel.tui.api.TuiApi
import de.htwg.sa.kniffel.tui.aview.TUI
import org.slf4j.{Logger, LoggerFactory}


object TuiService:
  val tui: TUI = TUI()

  given TUI = tui

  @main def main(): Unit =
    //Disable Logging
    LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
      .asInstanceOf[ch.qos.logback.classic.Logger]
      .setLevel(ch.qos.logback.classic.Level.ERROR)
    TuiApi().tui.run()