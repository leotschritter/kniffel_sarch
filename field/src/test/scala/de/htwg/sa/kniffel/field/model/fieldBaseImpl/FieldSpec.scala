package de.htwg.sa.kniffel.field.model.fieldBaseImpl

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class FieldSpec extends AnyWordSpec {
  "A Field" should {
    val field1 = new Field(4)
    "have a bar as String of form '+---+---+---+---+---+'" in {
      field1.bar() should be("+---+---+---+---+---+" + '\n')
    }
    "have a scalable bar" in {
      field1.bar(1, 1) should be("+-+-+" + '\n')
      field1.bar(1, 2) should be("+-+-+-+" + '\n')
      field1.bar(2, 1) should be("+--+--+" + '\n')
    }
    "have cells as String in form of ''" in {
      field1.cells() should be("|   |   |   |   |   |" + '\n')
    }
    "have scalable cells" in {
      field1.cells(1, 1, "", List.fill(1)("")) should be("| | |" + '\n')
      field1.cells(1, 2, "", List.fill(2)("")) should be("| | | |" + '\n')
      field1.cells(2, 1, "", List.fill(1)("")) should be("|  |  |" + '\n')
    }
    "have a header as List in Form of'List(    , |P1 , |P2 , |P3 , |P4 )'" in {
      field1.header() shouldBe a[List[String]]
      field1.header().mkString("") should be("    |P1 |P2 |P3 |P4 ")
    }
  }
  "get None in undoMove when Field is the first Field" in {
    var field = new Field(2)
    field = field.putMulti(List(1, 0, 1, 0, 1, 1), Some(1), 0, 0)
    field = field.undoMove(List(1, 0, 1, 0, 1, 1), 0, 0)
    field.matrix.cell(0, 0) should be(None)
  }
  "have a mesh as a String" in {
    val field2 = new Field(4)
    field2.mesh() should be(
      "    |P1 |P2 |P3 |P4 \n" +
        "+---+---+---+---+---+\n" +
        "|1  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|2  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|3  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|4  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|5  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|6  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|G  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|B  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|O  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|3x |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|4x |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|FH |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|KS |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|GS |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|KN |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|CH |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|U  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|O  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n" +
        "|E  |   |   |   |   |\n" +
        "+---+---+---+---+---+\n")
    field2.toString should be(field2.mesh())
  }

  "return Number of Players" in {
    val field = new Field(2)
    field.numberOfPlayers should be(2)
  }
  "when converted to JSON" should {
    "look like" in {
      val field: Field = new Field(2)
      field.toJson.toString should be(
        "{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]," +
          "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}")
    }
  }
  "when converted from JSON" should {
    "look like" in {
      val field: Field = new Field(2)
      field.jsonStringToField("{\"field\":{\"numberOfPlayers\":2,\"rows\":[[null,null],[null,null],[null,null],[null,null]," +
        "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]," +
        "[null,null],[null,null],[null,null],[null,null],[null,null],[null,null]]}}") should be(
        field)
    }
  }

  "when the newField function is called it" should {
    "return a new empty Field" in {
      var field: Field = new Field(2)

      field.newField(2) should be(field)
    }
  }
}



