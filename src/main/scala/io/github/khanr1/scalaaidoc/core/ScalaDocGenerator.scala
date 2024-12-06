// The documentation in this file has been generated via Generative AI

package io.github.khanr1.scalaaidoc.core

import cats.effect.kernel.{Async, Concurrent}
import cats.effect.kernel.Resource.ExitCase.Succeeded
import cats.syntax.all.*
import fs2.{text, Stream}
import fs2.io.file.{Path, Files, Flags, CopyFlags, CopyFlag}
import io.github.khanr1.scalaopenai.{OpenAI, OpenAIService, Roles}
import io.github.khanr1.scalaopenai.chat.Message
import org.typelevel.log4cats.Logger

/** Trait representing a ScalaDoc generator service.
  * 
  * The ScalaDoc generator is designed to create enriched documentation for 
  * a provided Scala source file.
  *
  * @tparam F
  *   The effect type, usually a monad supporting asynchronous and effectful computations.
  */
trait ScalaDocGenerator[F[_]]:

  /** Generates ScalaDoc for the specified file.
    *
    * This method reads the Scala source file at the given `path`, processes its
    * content, and produces enriched ScalaDoc documentation with additional inline comments.
    * 
    * @param path
    *   The `Path` of the Scala source file whose documentation is to be generated.
    * @return
    *   A stream producing the result of the ScalaDoc generation process,
    *   where the result might involve file outputs or logs.
    */
  def generateScalaDoc(path: Path): fs2.Stream[F, Nothing]

/** Companion object that provides mechanisms for creating and configuring instances 
  * of `ScalaDocGenerator`.
  */
object ScalaDocGenerator:

  /** Constructs a new instance of `ScalaDocGenerator`.
    *
    * The method uses the OpenAI API for content generation based on an input
    * Scala file. The implementation relies on effect type `F` which must 
    * support logging, file manipulation, and asynchronous processing.
    * 
    * @param apiKey
    *   A secret key for authenticating with the OpenAI API service.
    * @tparam F
    *   The effect type, often a type like `IO` from Cats Effect or other similar monads.
    * @return
    *   A constructed and ready-to-use `ScalaDocGenerator` instance.
    */
  def make[F[_]: Logger: Files: Async](apiKey: String): ScalaDocGenerator[F] =
    new ScalaDocGenerator[F] {
      
      /** Processes the given file and generates ScalaDoc using an AI-powered service.
        *
        * @param path
        *   The file path where the Scala source code is located.
        * @return 
        *   A stream that reads the file content, communicates with OpenAI for processing, 
        *   and writes the enhanced documentation back to the file or temporary output.
        */
      override def generateScalaDoc(path: Path): fs2.Stream[F, Nothing] =

        /** Generates a prompt for the OpenAI service based on the file's content.
          *
          * @param content
          *   An instance holding the content of the Scala source file.
          * @return
          *   A detailed prompt guiding the generative AI to add comments and documentation 
          *   for the file.
          */
        def prompt(content: FileContent): String = s"""
          |You are an AI assistant that specializes in Scala programming. 
          |Your task is to generate detailed and accurate ScalaDoc for the provided Scala code and add comments
          |where you think suitable to make the code more readable. You should not touch the code itself just the comments and the doc.
          |Please do not erase bracket; parenthesis and curly braket. Make sure that they are all closed
          |When you do so you need to put a comment on the
          |top of the file saying:
          |
          |
          |//The documentation in this file has been generated via Generative AI
          |
          |Your output should be raw text and should not contain marks like : ```

          |
          |Here is the code: ${content.value}
      """

        // Creates a stream resource for interacting with the OpenAI service.
        val streamedOpenAIResource: fs2.Stream[F, OpenAIService[F]] =
          fs2.Stream.resource(OpenAI.make[F](apiKey))

        // Reads the content of the target Scala file as a stream.
        val inputFileStream: fs2.Stream[F, FileContent] =
          FileProcessor
            .make[F]()
            .readScalaFile(path)

        /** Processes the content of the file and invokes the OpenAI service for augmented documentation.
          *
          * This process streams log messages to track progress and handles errors gracefully.
          *
          * @return
          *   A stream containing the result of the enriched content generation.
          */
        def processContent: fs2.Stream[F, String] =
          streamedOpenAIResource
            .flatMap { service =>
              inputFileStream
                .flatMap { content =>
                  val start = System.nanoTime() // Record processing start time
                  service
                    .chatCompletion(messages = List(Message(Roles.User, prompt(content))))
                    .map(_.getResponseMessage) // Extracts the response message from the AI service
                    .evalTap { chunk =>
                      Logger[F].info(s"Streaming in progress... Received chunk: ${chunk.take(50)}")
                    }
                }
            }
            .handleErrorWith(e =>
              fs2.Stream.eval(
                Logger[F].error(e.getMessage())
              ) *> fs2.Stream.empty // Log the error and produce an empty stream
            )

        // Defines the path for storing temporary output before it is applied to the original file.
        val outputPath = Path(path.toString + ".tmp")

        // Handles the integration of processed content back to the output file.
        processContent
          .through(text.utf8.encode) // Encode content into UTF-8 format
          .through(Files[F].writeAll(outputPath)) // Write enriched output to a temporary file
          .onFinalizeCase {
            case Succeeded =>
              // If process completes successfully, replace the original file with enriched content
              (Files[F].move(outputPath, path, CopyFlags(CopyFlag.ReplaceExisting))) *>
                Files[F].deleteIfExists(outputPath) *> 
                Logger[F].info(s"Successfully replaced ${path.fileName} with processed content")
            case _ =>
              // If processing fails, clean up temporary files and log a warning
              Files[F].deleteIfExists(outputPath) *> Logger[F].warn(
                s"Processing failed"
              )
          }
    }