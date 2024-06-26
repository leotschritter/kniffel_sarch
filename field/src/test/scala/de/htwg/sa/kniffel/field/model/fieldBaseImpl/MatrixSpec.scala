package de.htwg.sa.kniffel.field.model.fieldBaseImpl

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class MatrixSpec extends AnyWordSpec {
  "Matrix" when {
    "empty" should {
      "be created with specific dimension" in {
        val matrix = new Matrix(2)
        matrix.rows.size should be(19)
      }
      "for test purposes only be created with a Vector of Vectors" in {
        val testMatrix = Matrix(Vector(Vector(None)))
        testMatrix.rows.size should be(1)
      }
    }
    "filled" should {
      val matrix = new Matrix(2)
      "contain value" in {
        matrix.cell(0, 0) should be(None)
        matrix.rows(0)
      }
      "value should be insertable" in {
        val returnedMatrix = matrix.fill(1, 13, Some(73))
        returnedMatrix.cell(1, 13).get should be(73)
      }
    }
    "isEmpty" should {
      "return state of cell" in {
        val matrix = new Matrix(2)
        val m = matrix.fill(1, 13, Some(73))
        m.isEmpty(1, 13) should be(false)
        m.isEmpty(1, 1) should be(true)
      }
    }
  }
}
