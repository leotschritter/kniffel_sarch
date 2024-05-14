package de.htwg.sa.kniffel.persistence.persistence.fileIOXmlImpl.converter

import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}

import scala.xml.Elem

class JsonToXmlConverter {
  def fieldToXml(field: String): Elem = {
    val numberOfPlayers = (Json.parse(field) \ "field" \ "numberOfPlayers").as[Int]
    val rows = (Json.parse(field) \ "field" \ "rows").as[Array[JsValue]].map { outerValue =>
      outerValue.as[Array[JsValue]].map { innerVal =>
        innerVal.as[JsValue].match {
          case JsNumber(value) => Some(value.toInt)
          case _ => None
        }
      }.toList
    }.toList
    <field numberOfPlayers={numberOfPlayers.toString}>
      {(0 until numberOfPlayers).flatMap { col =>
      (0 until 19).map { row =>
        <cell row={row.toString} col={col.toString}>
          {rows(row)(col).map(cell => cell.toString).getOrElse("")}
        </cell>
      }
    }}
    </field>
  }

  def diceCupToXml(diceCup: String): Elem = {
    val lockedList = (Json.parse(diceCup) \ "dicecup" \ "stored").as[List[Int]]
    val inCupList = (Json.parse(diceCup) \ "dicecup" \ "incup").as[List[Int]]
    val remainingDices = (Json.parse(diceCup) \ "dicecup" \ "remainingDices").as[Int]

    val lockedDiceElements = lockedList.map(dice => <dice>
      {dice}
    </dice>)
    val inCupDiceElements = inCupList.map(dice => <dice>
      {dice}
    </dice>)

    <dicecup remainingDices={remainingDices.toString}>
      <locked quantity={lockedDiceElements.length.toString}>
        {lockedDiceElements}
      </locked>
      <incup quantity={inCupDiceElements.length.toString}>
        {inCupDiceElements}
      </incup>
    </dicecup>
  }

  def gameToXml(game: String): Elem = {
    val players = (Json.parse(game) \ "game" \ "players").as[Array[JsObject]]
    val nestedList = (Json.parse(game) \ "game" \ "nestedList").as[String].split(";").map(elem => elem.split(","))
    val remainingMoves = (Json.parse(game) \ "game" \ "remainingMoves").as[Int]
    val currentPlayerID = (Json.parse(game) \ "game" \ "currentPlayerID").as[Int]
    val currentPlayerName = (Json.parse(game) \ "game" \ "currentPlayerName").as[String]
    val playerElements = players.map { playerJson =>
      val playerID = (playerJson \ "id").as[Int].toString
      val playerName = (playerJson \ "name").as[String]
      <player playerid={playerID} playername={playerName}>
        <total>
          {nestedList(players.indexOf(playerJson)).head}
        </total>
        <bonus>
          {nestedList(players.indexOf(playerJson))(1)}
        </bonus>
        <total_of_upper_section>
          {nestedList(players.indexOf(playerJson))(2)}
        </total_of_upper_section>
        <total_of_lower_section>
          {nestedList(players.indexOf(playerJson))(3)}
        </total_of_lower_section>
        <grand_total>
          {nestedList(players.indexOf(playerJson)).last}
        </grand_total>
      </player>
    }

    <game remainingMoves={remainingMoves.toString} currentPlayerID={currentPlayerID.toString} currentPlayerName={currentPlayerName}>
      <scores>
        {playerElements}
      </scores>
    </game>
  }
}
