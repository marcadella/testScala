package mayeul.utils.parser.record.impl

import java.io.{BufferedReader, BufferedWriter}

import mayeul.utils.parser.record.{RecordReader, RecordWriter}

class LineWriter(out: BufferedWriter) extends RecordWriter[String](out) {
  override def writeEntry(record: String): Unit = printer.println(record)
}

class LineReader(in: BufferedReader) extends RecordReader[String](in) {
  override def readEntry(): Option[String] = readLine()
}
