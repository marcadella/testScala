package mayeul.utils.threads.heartbeat

import mayeul.utils.threads.DoEvery

import scala.concurrent.duration.Duration

class Heartbeat(period: Duration, todo: => Unit) extends DoEvery(period, todo) {
  override protected val prefix: String = "heartbeat"
}
