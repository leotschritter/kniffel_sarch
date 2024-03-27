package de.htwg.se.kniffel.util

trait Command[T, U]:
  def noStep(game: T, field: U): (T, U)

  def doStep(game: T, field: U): (T, U)

  def undoStep(game: T, field: U): (T, U)

  def redoStep(game: T, field: U): (T, U)

class UndoManager[T, U]:
  private var undoStack: List[Command[T, U]] = Nil
  private var redoStack: List[Command[T, U]] = Nil

  def doStep(game: T, field: U, command: Command[T, U]): (T, U) =
    undoStack = command :: undoStack
    redoStack = Nil
    command.doStep(game, field)

  def undoStep(game: T, field: U): (T, U) =
    undoStack match {
      case Nil => (game, field)
      case head :: stack => {
        val result = head.undoStep(game, field)
        undoStack = stack
        redoStack = head :: redoStack
        result
      }
    }

  def redoStep(game: T, field: U): (T, U) =
    redoStack match {
      case Nil => (game, field)
      case head :: stack => {
        val result = head.redoStep(game, field)
        redoStack = stack
        undoStack = head :: undoStack
        result
      }
    }
