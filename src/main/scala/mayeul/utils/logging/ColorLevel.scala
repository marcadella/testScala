package mayeul.utils.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.{
  ANSIConstants,
  ForegroundCompositeConverterBase
}

/**
  * Add <conversionRule conversionWord="colorLevel" converterClass="no.uit.metapipe.common.logging.ColorLevel" /> in your logback.xml
  * and use %colorLevel(...)
  */
class ColorLevel extends ForegroundCompositeConverterBase[ILoggingEvent] {

  override protected def getForegroundColorCode(
      event: ILoggingEvent): String = {
    val level: Level = event.getLevel
    level.toInt match {
      case Level.ERROR_INT =>
        /*ANSIConstants.BOLD + */
        ANSIConstants.RED_FG
      case Level.WARN_INT =>
        ANSIConstants.YELLOW_FG
      case Level.INFO_INT =>
        ANSIConstants.CYAN_FG
      case Level.DEBUG_INT =>
        ANSIConstants.MAGENTA_FG
      case _ =>
        ANSIConstants.DEFAULT_FG
    }
  }
}
