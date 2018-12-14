package mayeul.utils

import java.io._
import java.nio.file.{FileAlreadyExistsException, Files, Path}
import java.util.zip._

import mayeul.utils.json.Json
import mayeul.utils.logging.Logging
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.ReaderInputStream
import org.apache.commons.io.output.WriterOutputStream

import scala.io.Source
import scala.util.control.NonFatal
import org.apache.commons.io.{FileUtils => ApacheFileUtils}

object FileUtils extends Logging {

  /**
    * List all (directories, files) in a directory
    */
  def ls(path: Path): (Seq[Path], Seq[Path]) = {
    val d = new File(path.toString)
    if (d.exists && d.isDirectory) {
      val fl = d.listFiles.partition(_.isDirectory)
      (fl._1.map { _.toPath }, fl._2.map { _.toPath })
    } else {
      (Seq(), Seq())
    }
  }

  /**
    * Create necessary directories (if needed)
    * No effect if the whole path already exists
    */
  def createDirectories(path: Path): Unit = {
    if (!exists(path)) {
      if (!path.toFile.mkdirs()) {
        log.warn(
          s"Path '$path' was said to be non existing but did exist. Possible race condition.")
        /*throw new RuntimeException(
        s"Directories could not be created '${path.toAbsolutePath}'")*/
      }
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

  /**
    * Rename file.
    * Throws FileNotFoundException if not exist
    */
  def renameFile(path: Path, newFileName: String): Unit = {
    if (!isFile(path))
      throw new FileNotFoundException()
    else
      path.toFile.renameTo(path.getParent.resolve(newFileName).toFile)
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
    * Copy of files or non-empty directory
    * * 'to' is the path INCLUDING the file/directory re-name
    * Destination is created if does not exist
    * Throw FileAlreadyExistsException if destination already exists
    * Are you sure a symLink or hardLink would not be more efficient??
    */
  def hardCopy(from: Path, to: Path): Unit = {
    if (FileUtils.isDirectory(from))
      ApacheFileUtils.copyDirectory(from.toFile, to.toFile)
    else {
      FileUtils.createParentDirectories(to)
      Files.copy(from, to)
    }
  }

  def smartCopy(from: Path, to: Path): Unit = {
    hardLink(from, to)
  }

  /**
    * 'to' is the path INCLUDING the file/directory re-name
    * Creates parent directories if needed
    * Note: Works with non-empty directories as well
    */
  def moveAndRename(from: Path, to: Path): Unit = {
    createParentDirectories(to)
    Files.move(from, to)
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

  /**
    * Throws FileNotFoundException if not found
    */
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

  /**
    * Same as readFile but potentially unGzip it
    */
  def readGzipFile(path: Path): String = {
    val in = FileUtils.unGzipStream(
      new BufferedInputStream(new FileInputStream(path.toString)))
    try {
      IOUtils.toString(in, "UTF-8")
    } finally {
      in.close()
    }
  }

  def readFileAs[T: Manifest](path: Path): T = {
    Json.parse[T](readFile(path))
  }

  /**
    * Writes to a file.
    * Created if does not exist
    * If exists, the file is first deleted
    */
  def writeToFile(path: Path, str: String): Unit = {
    if (!isFile(path))
      createFile(path)
    else
      delete(path)
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
    * Stream to a file using an InputStream
    * Created if does not exist
    * The InputStream gets properly closed
    */
  def streamToFile(path: Path, is: InputStream): Unit = {
    if (!isFile(path))
      createFile(path)
    //We don't need to wrap with a BufferedOutputStream here since we copy only large chunks of data with IOUtils.copyLarge
    val os = new FileOutputStream(path.toAbsolutePath.toString)
    try {
      IOUtils.copyLarge(is, os)
    } finally {
      os.close()
      is.close()
    }
  }

  /**
    * Stream to a file using a Reader
    * Created if does not exist
    * The Reader gets properly closed
    */
  def streamToFile(path: Path, reader: Reader): Unit = {
    streamToFile(path, new ReaderInputStream(reader))
  }

  /**
    * Stream from a file using an OutputStream
    * Created if does not exist
    * The OutputStream gets properly closed
    */
  def streamFromFile(path: Path, os: OutputStream): Unit = {
    //We don't need to wrap with a BufferedInputStream here since we copy only large chunks of data with IOUtils.copyLarge
    val is = new FileInputStream(path.toAbsolutePath.toString)
    try {
      IOUtils.copyLarge(is, os)
    } finally {
      os.close()
      is.close()
    }
  }

  /**
    * Stream from a file using a Writer
    * The writer gets properly closed
    */
  def streamFromFile(path: Path, writer: Writer): Unit = {
    streamFromFile(path, new WriterOutputStream(writer))
  }

  /**
    * Unzip stream if zipped, otherwise returns the same stream
    * BufferedInputStream needed as we use reset()
    * Note: This method is NOT idempotent in the case where the stream was zipped as the ZIP header is read
    * (but is idempotent in the case where the stream was not)
    * The code commented out below is to unTar in case teh archive is composed only of one file. But it is not guaranty to work at all.
    */
  def unGzipStream(is: BufferedInputStream): InputStream = {
    is.mark(3) //2 bytes are read in the GZIPInputStream constructor
    try {
      val unzipped = new GZIPInputStream(is)
      /*val unzipped = new BufferedInputStream(new GZIPInputStream(is))
      //We remove the tar header
      unzipped.mark(513) //We read the first 512 characters
      val buff = Array.fill(512) { 0.toByte }
      if (unzipped.read(buff) < 512 || !buff.contains(0)) {
        unzipped.reset()
      }*/
      log.debug("Gzipped file")
      unzipped
    } catch {
      case _: ZipException =>
        log.debug("Not Gzipped file")
        is.reset()
        is
      case _: EOFException =>
        log.debug("Empty file -> considered as not gzipped")
        is.reset()
        is
    }
  }

  /**
    * Destroys the stream!!
    */
  private def isGzipStreamAndDestroy(is: InputStream): Boolean = {
    try {
      new GZIPInputStream(is) //We are already ready is in the constructor!
      true
    } catch {
      case _: ZipException => false
      case _: EOFException => false
    } finally {
      is.close()
    }
  }

  def isGzipped(path: Path): Boolean = {
    if (!isFile(path))
      throw new FileNotFoundException()
    else
      isGzipStreamAndDestroy(new FileInputStream(path.toFile))
  }

  /**
    * Zip all provided files
    * relativePath is used to relativise the paths in the zip. Otherwise the whole structure from root would be zipped.
    * No error if zip already exists: it will be overwritten
    */
  def zip(files: Seq[Path], out: Path, relativePath: Path): Unit = {
    def customRelativize(p: Path, root: Path): String = { //File.relativize cannot deal with two absolute files!
      p.toString.stripPrefix(root.toString).stripPrefix("/")
    }
    val zip = new ZipOutputStream(Files.newOutputStream(out))

    try {
      files.foreach { path =>
        zip.putNextEntry(new ZipEntry(customRelativize(path, relativePath)))
        Files.copy(path, zip)
        zip.closeEntry()
      }
    } finally {
      zip.close()
    }
  }

  /**
    * Zip all files in dirPath
    * No error if zip already exists: it will be overwritten
    * If deleteOrig = true: delete origin dir (dirPath)
    */
  def zip(dirPath: Path, out: Path, deleteOrig: Boolean = false): Unit = {
    if (!isDirectory(dirPath))
      throw new FileNotFoundException(
        s"$dirPath does not exist or is not a directory")
    zip(ls(dirPath)._2, out, dirPath)
    if (deleteOrig)
      FileUtils.deleteRecursiveDir(dirPath)
  }

  /**
    * Unzip the provided dir
    * No error if output files exist: they will be overwritten
    * If deleteOrig = true: delete origin zip (zipPath)
    */
  def unzip(zipPath: Path,
            out: Path,
            deleteOrig: Boolean = false,
            checkReady: Boolean = false): Unit = {
    import scala.collection.JavaConverters._

    if (!isFile(zipPath))
      throw new FileNotFoundException(
        s"$zipPath does not exist or is not a file")

    def unzipAllFile(entryList: List[ZipEntry],
                     inputGetter: ZipEntry => InputStream,
                     targetFolder: File): Boolean = {
      entryList match {
        case entry :: entries =>
          if (entry.isDirectory)
            new File(targetFolder, entry.getName).mkdirs
          else
            streamToFile(new File(targetFolder, entry.getName).toPath,
                         inputGetter(entry))

          unzipAllFile(entries, inputGetter, targetFolder)
        case _ =>
          true
      }

    }

    val zipFile = new ZipFile(zipPath.toString)

    val entriesList = zipFile.entries.asScala.toList
    unzipAllFile(entriesList, zipFile.getInputStream, new File(out.toString))
    if (deleteOrig)
      FileUtils.delete(zipPath)
    if (checkReady) {
      while (entriesList exists { entry =>
               !FileUtils.exists(out.resolve(entry.getName))
             }) {
        Thread.sleep(100)
        log.warn("One or more file has not yet been unzipped")
      }
    } else log.warn("All files unzipped")
  }

  /**
    * Destroys the stream!!
    */
  private def isZipStreamAndDestroy(is: InputStream): Boolean = {
    try {
      new ZipInputStream(is).getNextEntry != null //We are already reading in the constructor!
    } finally {
      is.close()
    }
  }

  def isZipped(path: Path): Boolean = {
    if (!isFile(path))
      throw new FileNotFoundException()
    else
      isZipStreamAndDestroy(new FileInputStream(path.toFile))
  }
}
