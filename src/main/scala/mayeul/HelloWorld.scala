package mayeul

import mayeul.utils.concurrency.{DoEvery, TimestampChecker}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
  }
}
