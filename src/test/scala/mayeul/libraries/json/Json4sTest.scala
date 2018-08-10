package mayeul

import org.scalatest.{FunSpec, Matchers}
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

//Same as lift-json

case class Child(name: String, age: Int, id: Option[Int])

class Json4sTest extends FunSpec with Matchers {
  implicit val formats = Serialization.formats(NoTypeHints)
  describe(classOf[Json4sTest].getName) {
    val c = Child("Mary", 5, None)
    val ser: String = write(c)
    val des: Child = read[Child](ser)
    it("should be able to serialize with None") {
      ser should be("""{"name":"Mary","age":5}""")
    }
    it("should be able to deserialize with one optional field missing") {
      des should be(c)
    }
    val c2 = Child("Mary", 5, Some(4))
    val ser2: String = write(c2)
    val des2: Child = read[Child](ser2)
    it("should be able to serialize with Some") {
      ser2 should be("""{"name":"Mary","age":5,"id":4}""")
    }
    it("should be able to deserialize with one optional field provided") {
      des2 should be(c2)
    }
    val raw = """{"name":"Mary","age":5, "color": "blue"}"""
    val des3: Child = read[Child](raw)
    it("should be able to deserialize with one field missing in case class") {
      des3 should be(c)
    }
  }
}
