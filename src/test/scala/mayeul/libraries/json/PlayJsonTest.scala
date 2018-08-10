package mayeul.libraries.json

import org.scalatest.{FunSpec, Matchers}

import play.api.libs.json._

case class Child(name: String, age: Int)

class PlayJsonTest extends FunSpec with Matchers {
  implicit val modelFormat = Json.format[Child]
  describe(classOf[PlayJsonTest].getName) {
    val c = Child("Mary", 5)
    val ser: String = """{"name":"Mary","age":5}"""
    val des: Child = Json.fromJson[Child](Json.parse(ser)).get
    it("should be able to deserialize") {
      des should be(c)
    }
    it("should find the format for implicit type") {
      B(ser).out should be(c)
    }
    it("should find the format for implicit type 2.0") {
      BB(ser).out should be(c)
    }
    it("should find the format for implicit type 3.0") {
      BBB(ser).out should be(c)
    }
  }
}

trait A {
  type T
}

case class B(ser: String)(implicit val k: Reads[Child]) extends A {
  type T = Child
  val out = Json.fromJson[T](Json.parse(ser)).get
}

trait AA {
  type T
  def reads: Reads[T]
  protected def outImpl(ser: String): T =
    Json.fromJson[T](Json.parse(ser))(reads).get
}

case class BB(ser: String)(implicit val k: Reads[Child]) extends AA {
  type T = Child
  val reads = k
  val out = outImpl(ser)
}

abstract class AAA[T] {
  protected def outImpl(ser: String)(implicit k: Reads[T]): T =
    Json.fromJson[T](Json.parse(ser)).get
}

case class BBB(ser: String)(implicit val k: Reads[Child]) extends AAA[Child] {
  val out = outImpl(ser)
}
