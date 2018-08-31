package mayeul.utils.logging

import com.typesafe.scalalogging.slf4j.{LazyLogging, Logger}
import org.slf4j.LoggerFactory

trait Logging extends LazyLogging { //We use lazy logging so that if the logger is not called it is not instantiated
  /**
    * Standard logger named after the name of the class it is mixed in.
    * Whether using Strict or Lazy logging, even with deep inheritance, the correct class name is used.
    */
  implicit protected lazy val log: Logger = logger

  /**
    * Same as log but add sub-names to differentiate between instances of a same class.
    * ("c" as custom)
    */
  def clog(s: String*): Logger = {
    Logger(LoggerFactory.getLogger((getClass.getName +: s.toSeq).mkString(".")))
  }
}
