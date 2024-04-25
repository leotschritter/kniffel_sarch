package de.htwg.sa.kniffel.controller.entity


import play.api.libs.json.{JsArray, JsNull, JsNumber, JsObject, JsValue, Json}

import scala.annotation.tailrec

case class Field(matrix: Matrix[Option[Int]]):
  def this(numberOfPlayers: Int) = this(new Matrix[Option[Int]](numberOfPlayers))

  private val defaultPlayers: Int = matrix.rows.flatten.length / 19

  def numberOfPlayers: Int = defaultPlayers


  def toJson: JsObject = {
    Json.obj(
      "field" -> Json.obj(
        "numberOfPlayers" -> JsNumber(this.numberOfPlayers),
        "rows" -> this.matrix.rows
      )
    )
  }


  def jsonStringToField(field: String): Field =
    val json: JsValue = Json.parse(field)
    val jsonRows: JsArray = (json \ "field" \ "rows").get.as[JsArray]

    val rows: Vector[Vector[Option[Int]]] = jsonRows.value.map { row =>
      row.as[JsArray].value.map {
        case JsNumber(value) => Some(value.toInt)
        case _ => None
      }.toVector
    }.toVector

    Field(Matrix(rows))