package mayeul.utils.concurrency

import scala.concurrent.{ExecutionContext, Future}

/**
  * Same as Runner without parameter
  */
class ParameterlessRunner[R](todo: => R)(implicit ec: ExecutionContext)
    extends Runner[Unit, R]((Unit) => todo) {
  override def execute(p: Unit = ()): Future[R] = {
    super.execute(p)
  }
}

object ParameterlessRunner {
  def apply[R](todo: => R)(implicit ec: ExecutionContext) =
    new ParameterlessRunner[R](todo)(ec)
}

/**
  * Same as CancellableRunner without parameter
  */
class CancellableParameterlessRunner[R](todo: => R, ec: ExecutionContext)
    extends CancellableRunner[Unit, R]((Unit) => todo, ec)

object CancellableParameterlessRunner {
  def apply[R](todo: => R)(implicit ec: ExecutionContext) =
    new CancellableParameterlessRunner[R](todo, ec)
}
