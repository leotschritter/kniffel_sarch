package de.htwg.se.kniffel
package controller.controllerBaseImpl

import model.Move
import model.fieldComponent.IField
import model.gameComponent.IGame
import util.Command

class SetCommand(move: Move) extends Command[IGame, IField] :

  override def doStep(game: IGame, field: IField): (IGame, IField) =
    val g = game.sum(move.value.toInt, move.y)
    (g, field.putMulti(g.getResultNestedList(move.x).map(_.toString), move.value, move.x, move.y))

  override def redoStep(game: IGame, field: IField): (IGame, IField) =
    val g = game.sum(move.value.toInt, move.y).next().get
    (g, field.putMulti(g.getResultNestedList(move.x).map(_.toString), move.value, move.x, move.y))

  override def undoStep(game: IGame, field: IField): (IGame, IField) =
    val g = game.undoMove(move.value.toInt, move.y)
    (g, field.undoMove(g.getResultNestedList(move.x).map(_.toString), move.x, move.y))

  override def noStep(game: IGame, field: IField): (IGame, IField) =
    (game, field)