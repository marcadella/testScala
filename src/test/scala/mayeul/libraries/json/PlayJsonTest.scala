package mayeul.libraries.json

import org.scalatest.{FunSpec, Matchers}

import play.api.libs.json._

case class Child3(name: String, age: Int)

class PlayJsonTest extends FunSpec with Matchers {
  implicit val modelFormat = Json.format[Child3]
  describe(classOf[PlayJsonTest].getName) {
    val c = Child3("Mary", 5)
    val ser: String = """{"name":"Mary","age":5}"""
    val des: Child3 = Json.fromJson[Child3](Json.parse(ser)).get
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

case class B(ser: String)(implicit val k: Reads[Child3]) extends A {
  type T = Child3
  val out = Json.fromJson[T](Json.parse(ser)).get
}

trait AA {
  type T
  def reads: Reads[T]
  protected def outImpl(ser: String): T =
    Json.fromJson[T](Json.parse(ser))(reads).get
}

case class BB(ser: String)(implicit val k: Reads[Child3]) extends AA {
  type T = Child3
  val reads = k
  val out = outImpl(ser)
}

abstract class AAA[T] {
  protected def outImpl(ser: String)(implicit k: Reads[T]): T =
    Json.fromJson[T](Json.parse(ser)).get
}

case class BBB(ser: String)(implicit val k: Reads[Child3]) extends AAA[Child3] {
  val out = outImpl(ser)
}
