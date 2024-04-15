package de.htwg.sa.kniffel
package controller.controllerBaseImpl

import de.htwg.sa.kniffel.field.IField
import de.htwg.sa.kniffel.game.IGame
import model.Move
import util.Command

class SetCommand(move: Move) extends Command[IGame, IField]:

  override def doStep(game: IGame, field: IField): (IGame, IField) =
    val g = game.sum(move.value, move.y)
    (g, field.putMulti(g.resultNestedList(move.x), Some(move.value), move.x, move.y))

  override def redoStep(game: IGame, field: IField): (IGame, IField) =
    game.sum(move.value, move.y).next().map(g =>
    (g, field.putMulti(g.resultNestedList(move.x), Some(move.value), move.x, move.y)))
      .getOrElse(game, field)
    /*game.sum(move.value.toInt, move.y).next()
      .match {
        case Some(g) => (g, field.putMulti(g.resultNestedList(move.x).map(_.toString), move.value, move.x, move.y))
        case None => (game, field)
      }*/

  override def undoStep(game: IGame, field: IField): (IGame, IField) =
    val g = game.undoMove(move.value, move.y)
    (g, field.undoMove(g.resultNestedList(move.x), move.x, move.y))

  override def noStep(game: IGame, field: IField): (IGame, IField) =
    (game, field)