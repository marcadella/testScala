package mayeul.utils.threads.heartbeat

import mayeul.utils.threads.DoEvery

import scala.concurrent.duration.Duration

class HeartbeatEmitter(period: Duration, todo: => Unit)
    extends DoEvery(period, todo)
