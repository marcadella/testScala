package mayeul.utils.concurrency

import scala.concurrent.{ExecutionContext, Future}

/**
  * Same as Runner without parameter
  */
class PlRunner[R](todo: => R)(implicit ec: ExecutionContext)
    extends Runner[Unit, R](Unit => todo)
    with PlRunnerLike[R] {
  def execute(): Future[R] = execute(())
}

object PlRunner {
  def apply[R](todo: => R)(implicit ec: ExecutionContext) =
    new PlRunner[R](todo)(ec)
}

/**
  * Same as CancellableRunner without parameter
  */
class CancellablePlRunner[R](todo: => R, ec: ExecutionContext)
    extends CancellableRunner[Unit, R](Unit => todo, ec)

object CancellablePlRunner {
  def apply[R](todo: => R)(implicit ec: ExecutionContext) =
    new CancellablePlRunner[R](todo, ec)
}
