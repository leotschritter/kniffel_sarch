package de.htwg.sa.kniffel
package aview

import controller.IController
import model.Move

import scala.swing.{Label, *}
import scala.swing.event.*
import scala.swing.ListView.*
import util.Event
import util.Observer
import aview.UI
import Config.given

import java.awt.Toolkit
import javax.swing.{ImageIcon, SpringLayout}
import javax.swing.border.Border

class GUI(using controller: IController) extends Frame, UI(controller) :
  controller.add(this)
  title = "Kniffel"
  iconImage = toolkit.getImage("src/main/resources/6.png")
  val tk: Toolkit = Toolkit.getDefaultToolkit
  val xSize: Int = tk.getScreenSize.getWidth.toInt
  val ySize: Int = tk.getScreenSize.getHeight.toInt

  size = new Dimension(xSize, ySize)
  background = new Color(255, 255, 255)
  repaint()
  val field_font: Font = new Font("Arial", 0, 20)
  val first_column_second_part: List[String] =
    List("gesamt", "<html>Bonus bei 63<br>oder mehr", "<html>gesamt<br>oberer Teil", "Dreierpasch", "Viererpasch", "Full-House",
      "Kleine Straße", "Große Straße", "Kniffel", "Chance", "<html>gesamt<br>unterer Teil", "<html>gesamt<br>oberer Teil", "Endsumme")

  val second_column: List[String] =
    List("<html>nur Einser<br>zählen", "<html>nur Zweier<br>zählen", "<html>nur Dreier<br>zählen", "<html>nur Vierer<br>zählen",
      "<html>nur Fünfer<br>zählen", "<html>nur Sechser<br>zählen", "→", "plus 35", "→", "<html>alle Augen<br>zählen",
      "<html>alle Augen<br>zählen", "<html>25<br>Punkte", "<html>30<br>Punkte",
      "<html>40<br>Punkte", "<html>50<br>Punkte", "<html>alle Augen<br>zählen", "→", "→", "→")

  val diceLinksField: List[String] = List("src/main/resources/3_mal_1.png", "src/main/resources/3_mal_2.png", "src/main/resources/3_mal_3.png",
    "src/main/resources/3_mal_4.png", "src/main/resources/3_mal_5.png", "src/main/resources/3_mal_6.png")

  val diceLinks: List[String] = List("src/main/resources/1.png", "src/main/resources/2.png", "src/main/resources/3.png",
    "src/main/resources/4.png", "src/main/resources/5.png", "src/main/resources/6.png")

  val intToImg: Map[Int, ImageIcon] = Map(1 -> new ImageIcon(diceLinks.head), 2 -> new ImageIcon(diceLinks(1)), 3 -> new ImageIcon(diceLinks(2)),
    4 -> new ImageIcon(diceLinks(3)), 5 -> new ImageIcon(diceLinks(4)), 6 -> new ImageIcon(diceLinks.last))

  val imgToInt: Map[String, Int] = Map(diceLinks.head -> 1, diceLinks(1) -> 2, diceLinks(2) -> 3,
    diceLinks(3) -> 4, diceLinks(4) -> 5, diceLinks.last -> 6)

  enum stateOfDices:
    case initial
    case running

  def update(e: Event): Unit = e match
    case Event.Quit => this.dispose()
    case Event.Save => contents
    case _ =>
      contents = new BorderPanel {
        add(new Label {
          opaque = true
          background = new Color(255, 255, 255)
        }, BorderPanel.Position.North)
        add(new LeftCellPanel(), BorderPanel.Position.West)
        add(new CenterCellPanel(), BorderPanel.Position.Center)
        add(new RightPanel(stateOfDices.running), BorderPanel.Position.East)
      }
      size = new Dimension(xSize, ySize)
      repaint()

  menuBar = new MenuBar {
    contents += new Menu("File") {
      contents += new MenuItem(Action("Exit") {
        sys.exit(0)
      })
      contents += new MenuItem(Action("Load") {
        controller.load
      })
      contents += new MenuItem(Action("Save") {
        controller.save
      })
    }
  }
  contents = new BorderPanel {
    add(new Label {
      text = "Welcome to Kniffel"
      opaque = true
      background = new Color(255, 255, 255)
    }, BorderPanel.Position.North)
    add(new LeftCellPanel(), BorderPanel.Position.West)
    add(new CenterCellPanel(), BorderPanel.Position.Center)
    add(new RightPanel(stateOfDices.initial), BorderPanel.Position.East)
  }
  pack()
  centerOnScreen()
  open()

  def field(numberOfPlayers: Int = controller.getField.numberOfPlayers): List[Label] =
    (for {
      i <- 0 until 19
      j <- 0 until numberOfPlayers
    } yield new Label {
      text = controller.getField.getMatrix.cell(j, i)
      font = field_font
      opaque = true
      if (j == getXIndex)
        background = new Color(239, 239, 239)
      else
        background = new Color(255, 255, 255)
      preferredSize = new Dimension(60, 20)
      border = Swing.LineBorder(new Color(0, 0, 0))
    }).toList

  def getXIndex: Int = controller.getGame.getPlayerID

  def isEmpty(y: Int): Boolean = controller.getField.getMatrix.isEmpty(getXIndex, y)

  def disableList: List[Int] = (for {y <- 0 until 19 if !isEmpty(y)} yield y).toList

  class RightPanel(state: stateOfDices, inCup: List[Int] = controller.getDicecup.getInCup,
                   locked: List[Int] = controller.getDicecup.getLocked, remaining_moves: Int = controller.getDicecup.getRemainingDices) extends BoxPanel(Orientation.Vertical) :
    contents += new RightUpperPanel
    contents += new RightBottomPanel
    contents += new BorderPanel {
      background = new Color(255, 255, 255)
    }
    contents += new BorderPanel {
      background = new Color(255, 255, 255)
    }
    contents += new BorderPanel {
      background = new Color(255, 255, 255)
    }

    class RightBottomPanel extends FlowPanel {
      background = new Color(255, 255, 255)
      contents += new UndoButton
      contents += new RedoButton
    }

    class RightUpperPanel extends BorderPanel {
      val right_font = new Font("Arial", 0, 15)
      val lstViewLeft: ListView[ImageIcon] = new ListView[ImageIcon]() {
        selection.intervalMode = IntervalMode.MultiInterval
        if (state.==(stateOfDices.running))
          listData = for (s <- controller.getDicecup.getInCup) yield intToImg(s)
        preferredSize = new Dimension(100, 500)
      }
      val lstViewRight: ListView[ImageIcon] = new ListView[ImageIcon]() {
        selection.intervalMode = IntervalMode.MultiInterval
        listData = for (s <- controller.getDicecup.getLocked) yield intToImg(s)
        preferredSize = new Dimension(100, 500)
      }
      add(new TopInnerPanel(), BorderPanel.Position.North)
      add(lstViewLeft, BorderPanel.Position.West)
      add(new RightInnerPanel(), BorderPanel.Position.Center)
      add(lstViewRight, BorderPanel.Position.East)
      add(new BottomPanel(), BorderPanel.Position.South)

      class BottomPanel() extends FlowPanel :
        background = new Color(255, 255, 255)
        border = Swing.MatteBorder(1, 0, 0, 0, new Color(0, 0, 0))
        contents += new Label {
          text = controller.getGame.getPlayerName + " ist an der Reihe."
          font = right_font
        }

      class TopInnerPanel() extends GridPanel(1, 3) :
        background = new Color(255, 255, 255)
        contents += new Label {
          text = "Im Becher"
          font = right_font
        }
        contents += new Label {
          text = "<html>Verbleibende<br>Würfe: " + (remaining_moves + 1)
          font = right_font
        }
        contents += new Label {
          text = "Rausgenommen"
          font = right_font
        }
        border = Swing.MatteBorder(0, 0, 1, 0, new Color(0, 0, 0))

      class RightInnerPanel() extends BoxPanel(Orientation.Vertical) {
        val buttonDimension: Dimension = new Dimension(90, 50)
        background = new Color(255, 255, 255)
        contents += new Button {
          icon = new ImageIcon("src/main/resources/right_arrow.png") {
            preferredSize = buttonDimension
          }
          preferredSize = buttonDimension
          listenTo(mouse.clicks)
          if (remaining_moves != 2)
            enabled = true
            reactions += {
              case MouseClicked(src, pt, mod, clicks, props) =>
                val intList: List[Int] = for (s <- lstViewLeft.selection.items.toList) yield imgToInt(s.toString)
                diceCupPutOut(intList)
            }
          else
            enabled = false
        }
        contents += new Button {
          icon = new ImageIcon("src/main/resources/left_arrow.png") {
            preferredSize = buttonDimension
          }
          preferredSize = buttonDimension
          listenTo(mouse.clicks)
          if (remaining_moves != 2)
            enabled = true
            reactions += {
              case MouseClicked(src, pt, mod, clicks, props) =>
                val intList: List[Int] = for (s <- lstViewRight.selection.items.toList) yield imgToInt(s.toString)
                diceCupPutIn(intList)
            }
          else
            enabled = false
        }
        val btn_dice: Button = new Button {
          icon = new ImageIcon("src/main/resources/flying_dices_small.png") {
            preferredSize = buttonDimension
          }
          preferredSize = buttonDimension
          listenTo(mouse.clicks)
          if (remaining_moves >= 0)
            reactions += {
              case MouseClicked(src, pt, mod, clicks, props) =>
                controller.doAndPublish(controller.dice())
            }
          else
            enabled = false
        }
        contents += btn_dice
        contents += new Label {
          icon = new ImageIcon("src/main/resources/dicecup_small.png")
          if (remaining_moves < 2)
            enabled = false
        }
        contents += new Label {
          icon = new ImageIcon("src/main/resources/dicecup_small.png")
          if (remaining_moves < 1)
            enabled = false
        }
        contents += new Label {
          icon = new ImageIcon("src/main/resources/dicecup_small.png")
          if (remaining_moves < 0)
            enabled = false
        }
      }
    }

  class LeftCellPanel() extends GridPanel(1, 2) :
    background = new Color(255, 255, 255)
    contents += new LeftCellPanelFirstColumn()
    contents += new LeftCellPanelSecondColumn()

  class LeftCellPanelFirstColumn(disableList: List[Int] = disableList) extends GridPanel(20, 1) :
    background = new Color(255, 255, 255)
    contents += new Label {
      text = ""
      opaque = true
      background = new Color(255, 255, 255)
    }
    for (i <- 0 until 6) {
      contents += new CellButton("", i, disableList.contains(i)) {
        icon = new ImageIcon(diceLinksField(i))
      }
    }
    for (i <- 0 until 13) {
      if (i < 3 || i > 9)
        contents += new Label {
          font = field_font
          //font = new Font("Arial", 0, 13)
          horizontalAlignment = Alignment.Center
          opaque = true
          background = new Color(255, 255, 255)
          text = first_column_second_part(i)
          xLayoutAlignment = 5.0
          border = Swing.LineBorder(new Color(0, 0, 0))
        }
      else
        contents += new CellButton("", i + 6, disableList.contains(i + 6)) {
          font = field_font
          //font = new Font("Arial", 0, 13)
          horizontalAlignment = Alignment.Center
          text = first_column_second_part(i)
          xLayoutAlignment = 5.0
          border = Swing.LineBorder(new Color(0, 0, 0))
        }
    }

  class LeftCellPanelSecondColumn() extends GridPanel(20, 1) :
    background = new Color(255, 255, 255)
    contents += new Label {
      text = ""
      opaque = true
      background = new Color(255, 255, 255)
    }
    for (i <- 0 until 19) {
      if (i == 6 || i == 8 || 15 < i && i < 19)
        contents += new Label {
          opaque = true
          background = new Color(255, 255, 255)
          icon = new ImageIcon("src/main/resources/right_arrow.png")
          border = Swing.LineBorder(new Color(0, 0, 0))
        }
      else
        contents += new Label {
          opaque = true
          background = new Color(255, 255, 255)
          text = second_column(i)
          font = field_font
          //font = new Font("Arial", 0, 13)
          horizontalAlignment = Alignment.Center
          border = Swing.LineBorder(new Color(0, 0, 0))
        }
    }

  class CenterCellPanel(numberOfPlayers: Int = controller.getField.numberOfPlayers) extends GridPanel(20, numberOfPlayers) :
    background = new Color(255, 255, 255)
    for (x <- 0 until numberOfPlayers) yield contents += new Label {
      text = controller.getGame.getPlayerName(x)
      font = field_font
      opaque = true
      foreground = new Color(255, 255, 255)
      background = new Color(0, 0, 0)
      border = Swing.LineBorder(new Color(0, 0, 0))
    }
    for (x <- field(numberOfPlayers)) yield contents += x

  class UndoButton() extends Button :
    icon = new ImageIcon("src/main/resources/undo.png")
    listenTo(mouse.clicks)
    reactions += {
      case MouseClicked(src, pt, mod, clicks, props)
      => controller.undo(); update(Event.Move)
    }

  class RedoButton() extends Button :
    icon = new ImageIcon("src/main/resources/redo.png")
    listenTo(mouse.clicks)
    reactions += {
      case MouseClicked(src, pt, mod, clicks, props)
      => controller.redo(); update(Event.Move)
    }

  class CellButton(value: String, y: Int, isDisabled: Boolean = true) extends Button(value) :
    if (!isDisabled)
      listenTo(mouse.clicks)
      reactions += {
        case MouseClicked(src, pt, mod, clicks, props)
        => writeDown(Move(getValue, getXIndex, y)); update(Event.Move)
      }
    else
      enabled = false

    // def errorMessage(): Unit = Dialog.showMessage(contents.head, "Feld ist schon belegt!", title = "Falsche Eingabe", messageType = Dialog.Message.Error)

    def getValue: String = controller.getDicecup.getResult(y).toString
