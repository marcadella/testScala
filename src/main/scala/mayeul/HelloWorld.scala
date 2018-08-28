package mayeul

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import java.util.Date

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization

object HelloWorld {
  implicit val ec = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val obj = new B
    println(obj.a)
    println(obj.aa)
    println(obj.b)
    println(obj.bb)
    println(obj.c)
    println(obj.cc)
    println(obj.d)
    println(obj.dd)
  }
}

trait A {
  val a: Int = 6
  val aa = a * a

  def b: Int = 7
  val bb = b * b

  def c: Int = 8
  val cc = c * c

  def d: Int = 9
  lazy val dd = d * d
}

class B extends A {
  override val a = 2

  override val b = 3

  override lazy val c = 4

  override val d = 5
}
