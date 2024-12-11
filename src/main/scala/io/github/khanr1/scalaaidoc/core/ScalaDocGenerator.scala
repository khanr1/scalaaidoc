//The documentation in this file has been generated via Generative AI

// Additional comments and ScalaDoc annotations have been added for clarity and improved understanding.

package io.github.khanr1.scalaaidoc.core

import cats.effect.kernel.{Async, Concurrent}
import cats.effect.kernel.Resource.ExitCase.Succeeded
import cats.syntax.all.*
import fs2.{text, Stream}
import fs2.io.file.{Path, Files, Flags, CopyFlags, CopyFlag}
import io.github.khanr1.scalaopenai.{OpenAI, OpenAIService, Roles}
import io.github.khanr1.scalaopenai.chat.Message
import org.typelevel.log4cats.Logger
import cats.syntax.validated

/** Trait defining the contract for generating ScalaDocs and project documentation for Scala source
  * files.
  *
  * This trait uses AI to generate documentation with three main functions:
  *   1. Generating ScalaDocs for a single file. 2. Generating ScalaDocs for all files in a project.
  *      3. Creating a summarized README file describing the project.
  *
  * @tparam F
  *   A higher-kinded type representing an effect type (e.g., IO), which supports asynchronous
  *   computations and resource safety.
  */
trait ScalaDocGenerator[F[_]]:

  /** Enhances a given Scala source file with improved ScalaDocs.
    *
    * The function reads the content of the given file, processes it through an AI-based engine
    * (e.g., OpenAI), generates richer ScalaDoc comments, and replaces the file with the updated
    * version.
    *
    * @param path
    *   Path to the Scala source file to be processed.
    * @return
    *   Stream effect representing the result of the operation with no emitted elements.
    */
  def generateScalaDoc(path: Path): fs2.Stream[F, Nothing]

  /** Creates a README.md file summarizing the overall project based on its Scala source files.
    *
    * This function analyzes all Scala source files in the given project, extracts relevant
    * information, and compiles it into a Markdown-based README file describing key details like
    * features and usage instructions.
    *
    * @param path
    *   Path to the root directory of the project whose README is generated.
    * @return
    *   Stream effect representing the generation process with no emitted elements.
    */
  def generateReadMe(path: Path): fs2.Stream[F, Nothing]

  /** Enhances all Scala files in the specified directory with improved ScalaDocs.
    *
    * The function iterates through every Scala source file within a given project directory,
    * refines their documentation using AI-based tooling, and updates the files in place.
    *
    * @param path
    *   Base directory containing Scala source files.
    * @return
    *   Stream effect representing the result of processing all files with no emitted elements.
    */
  def generateAllScalaDoc(path: Path): fs2.Stream[F, Nothing]

/** Companion object providing a concrete implementation of the `ScalaDocGenerator` trait. It
  * integrates dependencies such as OpenAI to handle interactions with external AI APIs.
  */
object ScalaDocGenerator:

  /** Constructs a `ScalaDocGenerator` instance, integrating OpenAI and file I/O functionalities.
    *
    * The created instance can handle tasks such as reading and writing code files, invoking OpenAI
    * APIs for generating ScalaDocs and project documentation, and logging results or errors.
    *
    * @param apiKey
    *   Authentication token required to interact with the OpenAI API.
    * @param F
    *   Implicit evidence for effect capabilities such as logging, asynchronous behavior, and file
    *   handling.
    * @tparam F
    *   Effect type used in this implementation (e.g., IO).
    * @return
    *   A concrete instance of `ScalaDocGenerator` for use in ScalaDoc generation tasks.
    */
  def make[F[_]: Logger: Files: Async](apiKey: String): ScalaDocGenerator[F] =
    new ScalaDocGenerator[F] {

      /** Implements ScalaDoc generation for a single file.
        *
        * Reads the Scala source file, enhances its documentation using AI (via OpenAI's API), and
        * updates the file.
        *
        * @param path
        *   The file path pointing to the Scala source file.
        */
      override def generateScalaDoc(path: Path): fs2.Stream[F, Nothing] =

        // Generates a prompt for the AI model to produce enhanced documentation for the given file.
        def prompt(content: FileContent): String = s"""
          |You are an AI assistant that specializes in Scala programming.
          |Your task is to generate concise and accurate ScalaDoc for the provided Scala code and add comments.
          |You should not touch the code itself, only add comments and edit the existing ScalaDoc.
          |Please do not erase brackets, parentheses, or curly braces— make sure they are  properly closed. 
          |Important: Please format the response as plain code without any markdown formatting like scala.
          |You also need to add the comment "//The documentation in this file has been generated via Generative AI" at the top of the file.
          |
          |Here is the code: ${content.value}
        """

        // Create a stream to initialize the OpenAI service resource.
        lazy val streamedOpenAIResource: fs2.Stream[F, OpenAIService[F]] =
          fs2.Stream.resource(OpenAI.make[F](apiKey))

        // Reads the content of the specified file.
        val inputFileStream: fs2.Stream[F, FileContent] =
          FileProcessor
            .make[F]()
            .readScalaFile(path)

        // Defines the processing pipeline for enhancing file content via AI.
        def processContent: fs2.Stream[F, Either[Throwable, String]] =
          streamedOpenAIResource
            .flatMap { service =>
              inputFileStream
                .map { content =>
                  service
                    .chatCompletion(messages = List(Message(Roles.User, prompt(content))))
                    .map(_.getResponseMessage) // Extract the generated documentation.
                }
                .parJoin(5)
            }
            .attempt
            .reduce((acc, next) => // If there are multiple responses, combine them
              (acc, next) match {
                case (Right(a), Right(b)) => Right(a + b) // Combine valid content
                case (Left(e), _) => Left(e) // Propagate the first error
                case (_, Left(e)) => Left(e)
              }
            )

        // Temporary file path for intermediate output, ensuring safety.
        val outputPath = Path(path.toString + ".tmp")

        processContent.flatMap {
          case Right(response) =>
            fs2.Stream
              .emit(response) // Proceed with valid responses
              .through(text.utf8.encode)
              .through(Files[F].writeAll(outputPath))
              .onFinalizeCase {
                case Succeeded =>
                  Files[F].move(outputPath, path, CopyFlags(CopyFlag.ReplaceExisting)) *>
                    Logger[F].info(s"Successfully updated ScalaDoc for ${path.fileName}")
                case _ =>
                  Files[F].deleteIfExists(outputPath) *> Logger[F].warn(
                    s"Failed to finalize file for ${path.fileName}"
                  )
              }
          case Left(error) =>
            fs2.Stream.eval(
              Logger[F].error(error)(
                s"Skipping file ${path.fileName} due to processing error: ${error.getMessage()}"
              )
            ) *> fs2.Stream.empty // Skip the write for this file
        }

      /** Implements README generation based on the contents of all Scala files in the project
        * directory.
        *
        * @param path
        *   Root directory of the Scala project.
        */
      override def generateReadMe(path: Path): Stream[F, Nothing] =

        lazy val streamedOpenAIResource: fs2.Stream[F, OpenAIService[F]] =
          fs2.Stream.resource(OpenAI.make[F](apiKey))

        lazy val fileProcessor: FileProcessor[F] = FileProcessor.make[F]()

        // Read all Scala files and combine their contents.
        lazy val fileContent: Stream[F, String] =
          fileProcessor
            .readAllScalaFiles(path)
            .map(_._2.value)
            .intersperse("\n--- New file---\n") // Add separators between file contents.
            .reduce(_ + _)

        // Prompt for generating the README based on all gathered file contents.
        def prompt(filescontent: String): String = s"""
          |You are an AI assistant specializing in Scala programming.
          |Your task is to create a high-quality README summarizing the entire project described by the following code:
          |$filescontent
        """

        val processContent =
          streamedOpenAIResource
            .flatMap(service =>
              fileContent
                .map(prompt(_))
                .flatMap(content =>
                  service.chatCompletion(messages = List(Message(Roles.User, content)))
                )
                .map(_.getResponseMessage) // Get the README content.
            )
            .handleErrorWith(e =>
              fs2.Stream.eval(
                Logger[F].error(e.getMessage()) // Log any errors.
              ) *> fs2.Stream.empty
            )

        processContent
          .through(text.utf8.encode) // Encode to UTF-8 bytes.
          .through(
            Files[F].writeAll(Path("./README.md"), Flags.Write)
          ) // Write the README to a file.

      /** Processes all files in the project directory, generating enriched ScalaDocs for each.
        *
        * @param path
        *   Base path of the project containing Scala source files.
        */
      override def generateAllScalaDoc(path: Path): Stream[F, Nothing] =
        val inputFileStream: fs2.Stream[F, (Path, FileContent)] =
          FileProcessor
            .make[F]()
            .readAllScalaFiles(path)

        // Process each file's path and content to enhance its ScalaDocs.
        inputFileStream.map((path, content) => generateScalaDoc(path)).parJoin(5)
    }
