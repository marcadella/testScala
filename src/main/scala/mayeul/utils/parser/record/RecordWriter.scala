package mayeul.utils.parser.record

import java.io._

/**
  * Writer for a record (serializer)
  * The assumptions are:
  * *   - a character stream
  * *   - where line separator plays a major role
  */
abstract class RecordWriter[-T](out: Writer) extends FilterWriter(out) {
  protected val printer = new PrintWriter(out)

  def writeEntry(record: T)

  final def writeAll(coll: TraversableOnce[T]): Unit = {
    coll.foreach(writeEntry)
    close()
  }

  override def close(): Unit = {
    printer.close()
    super.close()
  }
}
