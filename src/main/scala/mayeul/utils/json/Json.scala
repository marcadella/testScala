package mayeul.utils.json

import mayeul.utils.logging.Logging
import org.json4s.{Formats, NoTypeHints}
import org.json4s.jackson.Serialization

import scala.util.Try

class Json(formats: Formats) extends Logging {

  private implicit val fo: Formats = formats

  //Just limit for the error printout
  private val maxMsgSize: Int = 255
  private def truncate(s: String): String = {
    if (s.length > maxMsgSize)
      s.take(255) + "..."
    else
      s
  }

  def parse[T: Manifest](input: String): T = {
    val t: Try[T] = Try {
      Serialization.read[T](input)
    } recover {
      case e: Throwable =>
        log.warn(
          s"Error while parsing type ${implicitly[Manifest[T]].runtimeClass.getName} with input:\n${truncate(input)}",
          e)
        throw e
    }
    t.get
  }

  def serialize[T <: AnyRef: Manifest](input: T): String = {
    val t: Try[String] = Try {
      Serialization.write(input)
    } recover {
      case e: Throwable =>
        log.warn(
          s"Error while serializing type ${implicitly[Manifest[T]].runtimeClass.getName} with input:\n${truncate(
            input.toString)}",
          e)
        throw e
    }
    t.get
  }

  def prettyPrint[T <: AnyRef: Manifest](input: T): String = {
    val t: Try[String] = Try {
      Serialization.writePretty(input)
    } recover {
      case e: Throwable =>
        log.warn(
          s"Error while serializing type ${implicitly[Manifest[T]].runtimeClass.getName} with input:\n${truncate(
            input.toString)}",
          e)
        throw e
    }
    t.get
  }
}

object Json extends Json(Serialization.formats(NoTypeHints))
