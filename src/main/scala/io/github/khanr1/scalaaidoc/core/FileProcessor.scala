//The documentation in this file has been generated via Generative AI

/** Root package for core utilities in the Scala AI documentation library.
  *
  * This package contains the foundational abstractions and implementations for processing Scala
  * files in a concurrent and efficient manner using FS2 and Cats Effect.
  */
package io.github.khanr1.scalaaidoc.core

import cats.{Show, Eq, ApplicativeThrow}
import cats.effect.Concurrent
import cats.syntax.all._
import fs2.io.file.{Files, Path}
import fs2.Stream
import fs2.text
import fs2.text.utf8
import io.circe.{Encoder, Decoder}
import org.typelevel.log4cats.Logger

/** Trait representing a file processor capable of processing Scala files.
  *
  * The trait provides methods to read content from a single Scala file or multiple `.scala` files
  * in a directory. It ensures concurrent, streaming-safe processing using FS2 and typeclass
  * constraints provided by Cats Effect.
  *
  * @tparam F
  *   The effect type. Usually, this would be `IO` or similar. The typeclass requires `Concurrent`
  *   to ensure safe and efficient file processing in a concurrent environment.
  */
trait FileProcessor[F[_]: Concurrent]:

  /** Reads the content of a Scala file located at the specified path.
    *
    * The function outputs a stream of `FileContent` containing the file's content. Using a
    * streaming API helps in processing large files efficiently without loading the entire content
    * into memory.
    *
    * @param path
    *   The path to the `.scala` file which needs to be read.
    * @return
    *   A stream of `FileContent` objects corresponding to the file's content.
    */
  def readScalaFile(path: Path): fs2.Stream[F, FileContent]

  /** Reads the content of all `.scala` files in a specified directory.
    *
    * This function navigates through the provided directory path to find all `.scala` files
    * recursively, reads their content, and returns a stream of file paths paired with their
    * `FileContent`.
    *
    * @param path
    *   The directory path to search for `.scala` files.
    * @return
    *   A stream of tuples `(Path, FileContent)`, where each tuple contains the path to a `.scala`
    *   file and its content.
    */
  def readAllScalaFiles(path: Path): fs2.Stream[F, (Path, FileContent)]

/** Companion object providing functionality to create and manage `FileProcessor` instances.
  *
  * This object supplies a factory method that constructs a `FileProcessor` implementation using
  * FS2, Cats Effect, and Typelevel's logging abstraction (`Logger`).
  */
object FileProcessor:

  /** Creates a new `FileProcessor` instance for the effect type `F`.
    *
    * This function uses implicit instances of the `Files`, `Logger`, and `Concurrent` typeclasses
    * to manage file IO operations, log processing activities, and ensure safe concurrency.
    *
    * @tparam F
    *   The effect type, typically `IO` or similar, with Cats Effect concurrency capabilities.
    * @return
    *   A new `FileProcessor` instance.
    */
  def make[F[_]: Files: Logger: Concurrent](): FileProcessor[F] =
    new FileProcessor[F] {

      /** Reads and decodes the content of the given file path to `FileContent`.
        *
        * This method uses streaming to read file data in chunks and decode it to `.utf8` text. It
        * ensures that large files can be processed efficiently without consuming excessive memory.
        * Additionally, the read operation logs the time taken to read the file.
        *
        * @param path
        *   The path to the file to be read.
        * @return
        *   A stream of `FileContent` objects wrapping the file's content.
        */
      private def decodeAndMapToFileContent(path: Path): Stream[F, FileContent] =
        val start = System.nanoTime() // Record the start time for logging duration
        Files[F]
          .readAll(path)
          .through(text.utf8.decode)
          .map(FileContent(_))
          .evalTap(_ => Logger[F].info(s"File read in ${(System.nanoTime() - start) / 1e6} ms"))

      /** Filters a stream of paths to include only Scala files.
        *
        * The function checks the file paths for `.scala` extensions and excludes other types.
        *
        * @param stream
        *   The input stream containing file paths.
        * @return
        *   A filtered stream of paths only containing `.scala` files.
        */
      private def filterScalaFiles(stream: Stream[F, Path]): Stream[F, Path] =
        stream.filter(path => path.toString.endsWith(".scala"))

      /** Reads the content of a single `.scala` file.
        *
        * Before reading, the function validates that the file's path has a `.scala` extension. If
        * the extension is invalid, an error stream is raised.
        *
        * @param path
        *   The path to the `.scala` file.
        * @return
        *   A stream of `FileContent` containing the file's data if it has a valid extension.
        */
      override def readScalaFile(path: Path): fs2.Stream[F, FileContent] =
        // Validate `.scala` extension
        if path.extName == ".scala" then decodeAndMapToFileContent(path)
        else
          fs2.Stream
            .raiseError(
              new FileProcessorError.InvalidPathError(
                s"The path does not lead to a scala file: $path"
              )
            )
            .handleErrorWith(e => fs2.Stream.raiseError(e))

      /** Processes all `.scala` files within a directory and its subdirectories.
        *
        * The function first checks if the given path is a directory, then traverses it to find all
        * `.scala` files, reads their content, and pairs the file path with its `FileContent`. If
        * the provided path is not a directory, an error stream is raised.
        *
        * @param path
        *   The directory path to search for `.scala` files.
        * @return
        *   A stream of tuples `(Path, FileContent)` for each `.scala` file found.
        */
      override def readAllScalaFiles(path: Path): fs2.Stream[F, (Path, FileContent)] =
        // Log directory validation
        fs2.Stream.eval(Logger[F].info(s"Checking if the given path is a directory")) *>
          fs2.Stream.eval { Files[F].isDirectory(path) }.flatMap {
            case true =>
              Files[F]
                .walk(path) // Recursively walk through the directory
                .through(filterScalaFiles) // Filter only `.scala` files
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

/** Opaque type representing the content of a file as a string.
  *
  * This abstraction wraps a string and allows specific extension methods and utilities to be
  * defined on it. It enforces stronger type safety for file content manipulations.
  */
opaque type FileContent = String

/** Companion object for the opaque type `FileContent`.
  *
  * Provides utility methods for creating and manipulating instances of `FileContent`, along with
  * typeclass instances for JSON encoding, decoding, comparison, and string representation.
  */
object FileContent:

  /** Constructs a `FileContent` instance from a string.
    *
    * @param name
    *   The raw string representing the file content.
    * @return
    *   A new `FileContent` wrapping the provided string.
    */
  def apply(name: String): FileContent = name

  /** Provides extension methods for the `FileContent` opaque type. */
  extension (fileContent: FileContent)
    /** Retrieves the underlying string value from the `FileContent`.
      *
      * @return
      *   The raw string value contained in the `FileContent`.
      */
    def value: String = fileContent

  /** Implicit Circe encoder for `FileContent`.
    *
    * Encodes the `FileContent` into a JSON string.
    */
  given encoder: Encoder[FileContent] = fileContent => Encoder.encodeString.apply(fileContent.value)

  /** Implicit Circe decoder for `FileContent`.
    *
    * Decodes a JSON string into a `FileContent` instance.
    */
  given decoder: Decoder[FileContent] = Decoder.decodeString.map(apply)

  /** Implicit `Eq` instance for `FileContent`.
    *
    * Enables equality comparisons between `FileContent` instances.
    */
  given eqFileContent: Eq[FileContent] = Eq.fromUniversalEquals

  /** Implicit `Show` instance for `FileContent`.
    *
    * Converts the `FileContent` to a readable string representation for display purposes.
    */
  given showName: Show[FileContent] = Show.fromToString

/** Enum representing various types of errors that can occur during file processing. */
enum FileProcessorError extends Throwable:

  /** Error raised for an invalid file path.
    *
    * This error is typically raised when a path does not represent a `.scala` file or points to a
    * non-existent location.
    *
    * @param message
    *   A detailed description of the error.
    */
  case InvalidPathError(message: String) extends FileProcessorError

  /** Error raised if the provided path is not a directory.
    *
    * This error occurs when attempting to process files on a path that isn't a directory.
    *
    * @param message
    *   A detailed description of the error.
    */
  case DirectoryError(message: String) extends FileProcessorError