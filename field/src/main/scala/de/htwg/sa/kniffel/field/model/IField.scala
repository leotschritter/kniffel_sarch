package de.htwg.sa.kniffel.field.model

import akka.http.scaladsl.server.Route
import play.api.libs.json.JsObject

trait IField {
  def putMulti(valueList: List[Int], putInValue: Option[Int], x: Int, y: Int): IField

  def undoMove(valueList: List[Int], x: Int, y: Int): IField

  def numberOfPlayers: Int

  def matrix: IMatrix

  def toJson: JsObject

  def jsonStringToField(field: String): IField
  
  def newField(players: Int): IField
  
}

trait IMatrix {
  def cell(col: Int, row: Int): Option[Int]

  def isEmpty(col: Int, row: Int): Boolean
}