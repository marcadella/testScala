package mayeul

import java.nio.file.Paths

import no.uit.sfb.scalautils.common.FutureUtils
import no.uit.sfb.scalautils.json.Json

import scala.concurrent.{ExecutionContext, Future}


object HelloWorld {
  def main(args: Array[String]): Unit = {
    implicit val ec = ExecutionContext.global
    println("OK")
  }
}
