package mayeul.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.{
  ANSIConstants,
  ForegroundCompositeConverterBase
}

class LogbackHighlightingOverride
    extends ForegroundCompositeConverterBase[ILoggingEvent] {

  override protected def getForegroundColorCode(
      event: ILoggingEvent): String = {
    val level: Level = event.getLevel
    level.toInt match {
      case Level.ERROR_INT =>
        ANSIConstants.BOLD + ANSIConstants.RED_FG
      case Level.WARN_INT =>
        ANSIConstants.YELLOW_FG
      case Level.INFO_INT =>
        ANSIConstants.CYAN_FG
      case Level.DEBUG_INT =>
        ANSIConstants.BLUE_FG
      case _ =>
        ANSIConstants.DEFAULT_FG
    }
  }
}

class LogbackHighlightingOverride2
    extends ForegroundCompositeConverterBase[ILoggingEvent] {

  override protected def getForegroundColorCode(
      event: ILoggingEvent): String = {
    val level: Level = event.getLevel
    level.toInt match {
      case Level.ERROR_INT =>
        ANSIConstants.RED_FG
      case Level.WARN_INT =>
        ANSIConstants.YELLOW_FG
      case _ =>
        ANSIConstants.DEFAULT_FG
    }
  }
}
