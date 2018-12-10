package mayeul.utils.reactiveFsm

import rx.{Ctx, Rx}

object RxUtils {

  /**
    * Transforms a Seq[Rx[T]] into a Rx[Seq[T]]
    * The output reacts when any input changes
    */
  def sequence[T](seq: Seq[Rx[T]])(implicit ctx: Ctx.Owner): Rx[Seq[T]] =
    seq.foldLeft[Rx[Seq[T]]](Rx { Seq() }) {
      case (acc: Rx[Seq[T]], a: Rx[T]) =>
        Rx {
          acc() :+ a()
        }
    }
}
