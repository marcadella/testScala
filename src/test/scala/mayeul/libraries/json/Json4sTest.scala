package mayeul.libraries.json

import org.scalatest.{FunSpec, Matchers}
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

//Same as lift-json but can serialize Map[A, B] if A != String

case class Child2(name: String, age: Int, id: Option[Int], m: Map[Int, String])

class Json4sTest extends FunSpec with Matchers {
  implicit val formats = Serialization.formats(NoTypeHints)

  describe(classOf[Json4sTest].getName) {
    val c = Child2("Mary", 5, None, Map())
    it("should be able to serialize with None and empty Map") {
      val ser: String = write(c)
      ser should be("""{"name":"Mary","age":5,"m":{}}""")
    }
    it("should be able to deserialize with one optional field missing") {
      val ser: String = write(c)
      val des: Child2 = read[Child2](ser)
      des should be(c)
    }
    val c2 = Child2("Mary", 5, Some(4), Map(1 -> "a", 2 -> "b"))
    it("should be able to serialize with Some and non-empty Map") {
      val ser2: String = write(c2)
      ser2 should be("""{"name":"Mary","age":5,"id":4,"m":{"1":"a","2":"b"}}""")
    }
    it("should be able to deserialize with one optional field provided") {
      val ser2: String = write(c2)
      val des2: Child2 = read[Child2](ser2)
      des2 should be(c2)
    }
    it("should be able to deserialize with one field missing in case class") {
      val raw =
        """{"name":"Mary","age":5,"m":{}, "color": "blue"}"""
      val des3: Child2 = read[Child2](raw)
      des3 should be(c)
    }
  }
}
