package mayeul.utils

import java.io._
import java.nio.file.{FileAlreadyExistsException, Files, Path}
import java.util.zip.{GZIPInputStream, ZipException}

import mayeul.utils.json.Json
import org.apache.commons.io.IOUtils

import scala.io.Source
import scala.util.control.NonFatal
import org.apache.commons.io.{FileUtils => ApacheFileUtils}

object FileUtils {

  /**
    * Create necessary directories (if needed)
    * No effect if the whole path already existed
    */
  def createDirectories(path: Path): Unit = {
    if (!exists(path)) {
      if (!path.toFile.mkdirs())
        throw new RuntimeException(
          s"Directories could not be created '${path.toAbsolutePath}'")
    }
  }

  /**
    * Create necessary directories (if needed) of the parent of this path (useful is path is a file)
    */
  def createParentDirectories(path: Path): Unit = {
    createDirectories(path.getParent)
  }

  /**
    * Create new file and necessary directories (if needed)
    * Throws FileAlreadyExistsException if already exists
    */
  def createFile(path: Path): Unit = {
    createParentDirectories(path)

    if (!path.toFile.createNewFile())
      throw new FileAlreadyExistsException(
        s"File already exists '${path.toAbsolutePath}'")
  }

  def exists(path: Path): Boolean = {
    path.toFile.exists()
  }

  /**
    * Exists and is a file
    */
  def isFile(path: Path): Boolean = {
    path.toFile.isFile
  }

  /**
    * Exists and is a directory
    */
  def isDirectory(path: Path): Boolean = {
    path.toFile.isDirectory
  }

  def size(path: Path): Long = {
    if (!isFile(path))
      throw new RuntimeException(
        s"File ${path.toAbsolutePath} does not exist or is not a file")
    path.toFile.length()
  }

  /**
    * Delete file or empty directory
    */
  def delete(path: Path): Unit = {
    if (exists(path)) {
      if (!path.toFile.delete())
        throw new RuntimeException(
          s"Deletion of file ${path.toAbsolutePath} failed")
    }
  }

  /**
    * Delete directory recursively
    * Does nothing if des not exist
    */
  def deleteRecursiveDir(path: Path): Unit = {
    ApacheFileUtils.deleteDirectory(path.toFile)
  }

  /**
    * Destination is created if does not exist
    * Are you sure a symLink or hardLink would not be more efficient??
    */
  def fullCopy(from: Path, to: Path): Unit = {
    if (from != to) {
      if (!isFile(to))
        createFile(to)
      streamToFile(to, new FileInputStream(from.toAbsolutePath.toString))
    }
  }

  def smartCopy(from: Path, to: Path): Unit = {
    hardLink(from, to)
  }

  def move(from: Path, to: Path): Unit = {
    if (from != to) {
      try {
        hardLink(from, to)
      } catch {
        case NonFatal(_) =>
          fullCopy(from, to)
      }
      delete(from)
    }
  }

  /**
    * Can point to a directory or file and cross filesystem boundaries or span across partitions
    * If the source gets deleted, the data is lost
    * The parent directories are created if needed
    */
  def symLink(source: Path, link: Path): Unit = {
    if (link != source) {
      createParentDirectories(link)
      Files.createSymbolicLink(link, source)
    }
  }

  /**
    * Can only point to a file and cannot cross filesystem boundaries or span across partitions
    * The data is still available after the deletion of the source
    * The parent directories are created if needed
    */
  def hardLink(source: Path, link: Path): Unit = {
    if (link != source) {
      createParentDirectories(link)
      Files.createLink(link, source)
    }
  }

  def readFile(path: Path): String = {
    val reader = Source.fromFile(path.toAbsolutePath.toString)
    try {
      reader.getLines.mkString("\n")
    } catch {
      case NonFatal(_) =>
        throw new RuntimeException(
          s"Could not read from file ${path.toAbsolutePath}")
    } finally {
      reader.close()
    }
  }

  def readFileAs[T: Manifest](path: Path): T = {
    Json.parse[T](readFile(path))
  }

  /**
    * Writes to a file.
    * Created if does not exist
    */
  def writeToFile(path: Path, str: String): Unit = {
    if (!isFile(path))
      createFile(path)
    val file = path.toFile
    val writer = new PrintWriter(file)
    try {
      writer.write(str)
    } catch {
      case NonFatal(_) =>
        throw new RuntimeException(
          s"Could not write to file ${path.toAbsolutePath}")
    } finally {
      writer.close()
    }
  }

  def writeToFileAs[T <: AnyRef: Manifest](path: Path, obj: T): Unit = {
    writeToFile(path, Json.serialize[T](obj))
  }

  /**
    * Stream to a file
    * Created if does not exist
    * The InputStream gets properly closed
    */
  def streamToFile(path: Path, is: InputStream): Unit = {
    if (!isFile(path))
      createFile(path)
    val os = new FileOutputStream(path.toAbsolutePath.toString)
    try {
      IOUtils.copyLarge(is, os)
    } finally {
      os.close()
      is.close()
    }
  }

  def isGzipped(path: Path): Boolean = {
    if (!isFile(path))
      throw new FileNotFoundException()
    else {
      val f = new FileInputStream(path.toFile)
      try {
        val gzip = new GZIPInputStream(f)
        gzip.close()
        true
      } catch {
        case _: ZipException => false
      } finally {
        f.close()
      }
    }
  }
}
