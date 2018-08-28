package mayeul.libraries.json

import org.scalatest.{FunSpec, Matchers}

import play.api.libs.json._

class PlayJsonTest extends FunSpec with Matchers {
  implicit val modelFormat = Json.format[ChildReduced]
  describe(classOf[PlayJsonTest].getName) {
    val c = ChildReduced("Mary", 5)
    val ser: String = """{"name":"Mary","age":5}"""
    val des: ChildReduced = Json.fromJson[ChildReduced](Json.parse(ser)).get
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

case class B(ser: String)(implicit val k: Reads[ChildReduced]) extends A {
  type T = ChildReduced
  val out = Json.fromJson[T](Json.parse(ser)).get
}

trait AA {
  type T
  def reads: Reads[T]
  protected def outImpl(ser: String): T =
    Json.fromJson[T](Json.parse(ser))(reads).get
}

case class BB(ser: String)(implicit val k: Reads[ChildReduced]) extends AA {
  type T = ChildReduced
  val reads = k
  val out = outImpl(ser)
}

abstract class AAA[T] {
  protected def outImpl(ser: String)(implicit k: Reads[T]): T =
    Json.fromJson[T](Json.parse(ser)).get
}

case class BBB(ser: String)(implicit val k: Reads[ChildReduced])
    extends AAA[ChildReduced] {
  val out = outImpl(ser)
}
