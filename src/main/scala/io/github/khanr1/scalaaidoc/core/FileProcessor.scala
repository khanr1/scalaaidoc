// The documentation in this file has been generated via Generative AI

package io.github.khanr1.scalaaidoc.core

import cats.{Show, Eq, ApplicativeThrow}
import cats.effect.Concurrent
import cats.syntax.all.*
import fs2.io.file.{Files, Path}
import fs2.Stream
import fs2.text
import fs2.text.utf8
import io.circe.{Encoder, Decoder}
import org.typelevel.log4cats.Logger

/** Trait representing a file processor capable of processing Scala files. This includes functionality
  * for reading individual files or all Scala files in a directory.
  *
  * @tparam F
  *   The effect type. Commonly, this will be an `IO` or similar effect type that provides
  *   concurrent, resource-safe computation, such as those from Cats Effect.
  */
trait FileProcessor[F[_]: Concurrent]:

  /** Reads the content of a Scala file at a specified path.
    *
    * The function reads the file's content as a stream of `FileContent`, a specialized
    * type that wraps a string representing the file's content.
    *
    * @param path
    *   The path to the Scala file to be read.
    * @return
    *   A stream of `FileContent` containing the file's content.
    */
  def readScalaFile(path: Path): fs2.Stream[F, FileContent]

  /** Reads the content of all Scala files within a given directory (and its subdirectories).
    *
    * This function filters files with `.scala` extensions and reads the content of each one,
    * returning a stream of tuples where each tuple consists of the `Path` and its corresponding `FileContent`.
    *
    * @param path
    *   The path of the directory to search for Scala files.
    * @return
    *   A stream of tuples, with each tuple containing the file's `Path` and `FileContent`.
    */
  def readAllScalaFiles(path: Path): fs2.Stream[F, (Path, FileContent)]

/** Companion object for `FileProcessor`, providing a factory method to create instances of the trait. */
object FileProcessor:

  /** Constructs a new instance of `FileProcessor` using the provided typeclass instances.
    *
    * @tparam F
    *   The effect type, commonly an `IO` or similar.
    * @param F
    *   The implicit `Files`, `Logger`, and `Concurrent` typeclass instances required for file processing,
    *   logging, and concurrency.
    * @return
    *   A new instance of `FileProcessor`.
    */
  def make[F[_]: Files: Logger: Concurrent](): FileProcessor[F] =
    new FileProcessor[F] {

      /** Reads and decodes the contents of a file, mapping it to a `FileContent` instance.
        *
        * This method uses FS2 to perform file reads in a streaming manner, ensuring resource safety and scalability.
        *
        * @param path
        *   The path to the file to be read.
        * @return
        *   A stream of `FileContent` containing the decoded content of the file.
        */
      private def decodeAndMapToFileContent(path: Path): Stream[F, FileContent] =
        val start = System.nanoTime()
        Files[F]
          .readAll(path)
          .through(text.utf8.decode)
          .map(FileContent(_))
          .evalTap(_ => Logger[F].info(s"File read in ${(System.nanoTime() - start) / 1e6} ms"))

      /** Filters a stream of file paths to only include Scala files (files with a `.scala` extension).
        *
        * @param stream
        *   The stream of file paths to be filtered.
        * @return
        *   A filtered stream containing only paths to Scala files.
        */
      private def filterScalaFiles(stream: Stream[F, Path]): Stream[F, Path] =
        stream.filter(path => path.toString.endsWith(".scala"))

      /** Reads the content of a single Scala file at the provided path. Validates that the file has a `.scala` extension
        * before attempting to read its content.
        *
        * @param path
        *   The path to the Scala file to be read.
        * @return
        *   A stream of `FileContent` containing the content of the file.
        */
      override def readScalaFile(path: Path): fs2.Stream[F, FileContent] =
        // Validates that the file has a `.scala` extension before proceeding
        if path.extName == ".scala" then decodeAndMapToFileContent(path)
        else
          fs2.Stream
            .raiseError(
              new FileProcessorError.InvalidPathError(
                s"The path does not lead to a scala file: $path"
              )
            )
            .handleErrorWith(e => fs2.Stream.raiseError(e))

      /** Reads all Scala files from the provided directory path. Walks through the directory structure recursively,
        * filters for Scala files (.scala), and reads their content. Errors if the provided path is not a directory.
        *
        * @param path
        *   The directory path to search for Scala files.
        * @return
        *   A stream of tuples, where each tuple contains a file `Path` and its corresponding `FileContent`.
        */
      override def readAllScalaFiles(path: Path): fs2.Stream[F, (Path, FileContent)] =
        // Logs the initial check to see if the provided path is a directory
        fs2.Stream.eval(Logger[F].info(s"Checking if the given path is a directory")) *>
          fs2.Stream.eval { Files[F].isDirectory(path) }.flatMap {
            case true =>
              Files[F]
                .walk(path)
                .through(filterScalaFiles)
                .flatMap { path =>
                  decodeAndMapToFileContent(path).map(content => (path, content))

                }
                .evalTap { (path, content) =>
                  Logger[F].info(s"Starting to process file ${path.fileName}")
                }
            case false =>
              fs2.Stream
                .raiseError[F](
                  FileProcessorError.DirectoryError(s"The path: $path is not a directory")
                )
          }
    }

/** Opaque type representing the content of a file as a string. This abstraction allows encapsulation
  * of file content while still treating it as a string internally.
  */
opaque type FileContent = String

/** Companion object for `FileContent` providing constructors and utility functions for working
  * with file contents.
  */
object FileContent:

  /** Constructs a `FileContent` instance from a string.
    *
    * @param name
    *   The string content of the file.
    * @return
    *   A `FileContent` instance wrapping the provided string.
    */
  def apply(name: String): FileContent = name

  /** Provides extension methods for `FileContent`. */
  extension (fileContent: FileContent)

    /** Returns the underlying string content of the `FileContent`.
      *
      * @return
      *   The string content of the file.
      */
    def value: String = fileContent

  /** JSON encoder instance for `FileContent` using Circe. Encodes the content as a JSON string. */
  given encoder: Encoder[FileContent] = fileContent => Encoder.encodeString.apply(fileContent.value)
  /** JSON decoder instance for `FileContent` using Circe. Decodes file content from a JSON string. */
  given decoder: Decoder[FileContent] = Decoder.decodeString.map(apply)
  /** Equality instance for `FileContent` for comparisons, using universal equality. */
  given eqFileContent: Eq[FileContent] = Eq.fromUniversalEquals
  /** Show instance for `FileContent`. Converts the content to its string representation for display. */
  given showName: Show[FileContent] = Show.fromToString

/** Enum defining various types of errors that may occur during file processing. */
enum FileProcessorError extends Throwable:

  /** Error indicating an invalid file path. This typically occurs when the path provided
    * is not a `.scala` file or does not exist.
    *
    * @param message
    *   The error message describing the issue.
    */
  case InvalidPathError(message: String) extends FileProcessorError

  /** Error indicating that the provided path is not a directory. This typically occurs
    * when attempting to process Scala files using a non-directory path.
    *
    * @param message
    *   The error message describing the issue.
    */
  case DirectoryError(message: String) extends FileProcessorError