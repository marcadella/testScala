package mayeul.utils.parser.record

import java.io.{BufferedReader, FilterReader}

import scala.collection.mutable.ListBuffer

/**
  * Reader for reading a record (i.e. a parser)
  * The assumptions are:
  *   - a character stream
  *   - where line separator plays a major role
  */
abstract class RecordReader[T](in: BufferedReader) extends FilterReader(in) {

  /**
    * Reads the next entry
    * Returns None if EOF
    */
  def readEntry(): Option[T]

  protected[record] def readLineImpl(): Option[String] = {
    Option(in.readLine())
  }

  /**
    * Reads the next line
    * Returns None if EOF
    */
  protected def readLine(): Option[String] = {
    readLineImpl()
  }

  override def close(): Unit = {
    in.close()
    super.close()
  }
}

/**
  * Reader for reading a record (i.e. a parser) with unreadLine() method for more complex parsing situations
  */
abstract class RecordReaderWithUnread[T](in: BufferedReader)
    extends RecordReader[T](in) {

  /**
    * Memory to implement unreadLine()
    * If line = None -> initial state
    * If future = false -> normal reading
    * If future = true -> unread a line (line is the line that was unread)
    * Trying to unread when future = true -> Error
    */
  case class Memory(future: Boolean = false, line: Option[String] = None)

  protected var _memory: Memory = Memory()
  protected var _nextLine: Option[String] = readLineImpl()

  /**
    * Reads the next line
    * Returns None if EOF
    */
  final override protected def readLine(): Option[String] = {
    synchronized {
      if (!_memory.future) { //Normal read
        _memory = Memory(false, _nextLine)
        _nextLine = readLineImpl()
      } else { //Read after an unread
        val futureLine = _memory.line
        _memory = Memory(false, _nextLine)
        _nextLine = futureLine
      }
      _memory.line
    }
  }

  final protected def unreadLine(): Unit = {
    synchronized {
      if (_memory.future)
        new RuntimeException("Cannot unread twice in a raw the stream")
      if (_memory.line.isEmpty)
        new RuntimeException(
          "Cannot unread stream before reading at least one line")
      val futureLine = _nextLine
      _nextLine = _memory.line
      _memory = Memory(true, futureLine)
    }
  }
}

/**
  * Iterator API wrapper for a record Reader
  */
class RecordReaderIterator[T](in: RecordReader[T]) extends Iterator[T] {
  protected var _nextEntry: Option[T] = in.readEntry()

  final def next(): T = {
    synchronized {
      val entry = _nextEntry
      _nextEntry = in.readEntry()
      entry
    }.getOrElse(throw new RuntimeException("Next on empty iterator"))
  }

  final def hasNext: Boolean = {
    val hasNext = synchronized {
      _nextEntry.nonEmpty
    }
    if (!hasNext)
      in.close()
    hasNext
  }

  /**
    * Load all in memory
    */
  final def readAll(): Seq[T] = {
    val buffer = ListBuffer[T]()
    while (hasNext) buffer += next()
    buffer.toList
  }

  /**
    * Transforms an input into an output with a transformation keeping all the elements in between
    * Stream implementation
    */
  def transform[S](transform: T => S, out: RecordWriter[S]): Unit = {
    out.writeAll(this.map { transform })
  }

  /**
    * Transforms an input record into an output record with a transformation/filter in between
    * Stream implementation
    */
  def transformAndFilter[S](transform: T => Option[S],
                            out: RecordWriter[S]): Unit = {
    out.writeAll(this.flatMap { t =>
      transform(t)
    })
  }
}
