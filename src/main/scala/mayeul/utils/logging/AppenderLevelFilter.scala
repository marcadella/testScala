package mayeul.utils.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

/**
  * Add to appender to filter out events.
  * Note: empty logger is not recognized. Use "." if you mean root
  * Note: The order matters! Start with the most specific logger to finish with least specific.
  */
class AppenderLevelFilter extends Filter[ILoggingEvent] {
  private var level: Level = _
  private var logger: String = _

  override def decide(event: ILoggingEvent): FilterReply = {
    if (!isStarted) {
      FilterReply.NEUTRAL
    } else {
      val eventLoggerParts = event.getLoggerName.split('.').filter(_.nonEmpty)
      val loggerParts = logger.split('.').filter(_.nonEmpty)
      if (eventLoggerParts.startsWith(loggerParts)) {
        if (event.getLevel.isGreaterOrEqual(level))
          FilterReply.ACCEPT
        else
          FilterReply.DENY
      } else
        FilterReply.NEUTRAL
    }
  }

  def setLevel(level: Level) {
    this.level = level
  }

  def setLogger(logger: String) {
    this.logger = logger
  }

  override def start() {
    if (this.level != null && this.logger != null) {
      super.start()
    }
  }
}
