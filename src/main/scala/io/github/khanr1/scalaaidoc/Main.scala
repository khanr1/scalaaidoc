//The documentation in this file has been generated via Generative AI
package io.github.khanr1
package scalaaidoc

import core.*

import io.github.khanr1.scalaopenai.Config.*
import cats.syntax.all.*
import cats.effect.{IO, IOApp}
import fs2.io.file.Path
import io.github.khanr1.scalaaidoc.core.FileProcessor.make
import io.github.khanr1.scalaopenai.OpenAIConfig
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.Logger.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.io.StdIn

/** Main object for the application. This is the entry point for the application and handles the
  * menu logic to provide available functionalities like generating ScalaDoc and generating a
  * ReadMe.md file.
  */
object Main extends IOApp.Simple:

  // The logging functionality using an implicit logger. The Slf4jLogger implementation is utilized here.
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  /** Displays the menu options to the user.
    *
    * Prompts the user with available choices and describes the application's functionality.
    *
    * @return
    *   An `IO[Unit]` effect which, when evaluated, prints the menu options to the console.
    */
  def showMenu: IO[Unit] = IO.println(
    """
      | Choose an Option: 
      | 1. Generate ScalaDoc
      | 2. Generate ReadMe.Md
      | 3. exit
    """
  )

  /** Retrieves the OpenAI API key configuration by loading it from a supported configuration
    * source.
    *
    * @return
    *   An `fs2.Stream` containing the OpenAI configuration object with credentials necessary for
    *   API calls.
    */
  val apikey: fs2.Stream[IO, OpenAIConfig] = fs2.Stream.eval(loadOpenAIConfig().load[IO])

  /** Represents the input path where Scala sources are located.
    *
    * This path is considered the root directory for source code file processing.
    */
  val inputPath: Path = Path(
    "/Users/raphaelkhan/Developer/scalaai-doc/src/main/scala/io/github/khanr1/scalaaidoc"
  )

  /** Handles the functionality to generate ScalaDoc for all the Scala files present in the
    * `inputPath`.
    *
    * Uses the OpenAI API to process and generate the documentation from source files sequentially.
    *
    * @return
    *   An `IO[Unit]` effect that evaluates and generates ScalaDocs for the Scala files in the
    *   specified directory.
    */
  def generateScalaDocs(path: Path): IO[Unit] = apikey
    .flatMap { key => ScalaDocGenerator.make[IO](key.apiKey).generateAllScalaDoc(path) }
    .compile
    .drain

  /** Handles the functionality to generate a ReadMe.md file based on the Scala source files in the
    * `inputPath`.
    *
    * Combines information from source files and presents it in the form of a structured README
    * file.
    *
    * @return
    *   An `IO[Unit]` effect that generates the ReadMe.md document and completes once it is created.
    */
  def generateReadME(path: Path): IO[Unit] = apikey
    .flatMap(key => ScalaDocGenerator.make[IO](key.apiKey).generateReadMe(path))
    .compile
    .drain

  /** Runs the main menu loop to process user input and execute corresponding functionality.
    *
    * Provides menu-driven logic, allowing users to interact with the application via a choice-based
    * interface. Continuously shows the menu options until the user chooses to exit the application.
    *
    * @return
    *   An `IO[Unit]` effect that continuously handles the menu-driven execution.
    */
  def menuLoop: IO[Unit] = {

    /** Reads user's input and attempts to parse it as an integer.
      *
      * If the input cannot be parsed, defaults to -1 to indicate invalid input.
      *
      * @return
      *   An `IO[Int]` effect representing the numeric choice input by the user.
      */
    def getChoice: IO[Int] =
      IO(Option(StdIn.readLine("Enter you Choice: ")).flatMap(_.toIntOption).getOrElse(-1))

    def getPath: IO[Path] =
      IO {
        val input = StdIn.readLine("enter path: ")
        Path(input.trim())
      }
    // Continuously performs user input parsing and associated action handling.
    for
      _ <- showMenu // Show menu options
      choice <- getChoice // Get user choice
      _ <- choice match {
        case 1 =>
          for
            path <- getPath
            _ <- generateScalaDocs(path) *> menuLoop
          yield () // Option 1: Generate ScalaDoc, then loop back
        case 2 =>
          for
            path <- getPath
            _ <- generateReadME(path) *> menuLoop
          yield () // Option 2: Generate README, then loop back
        case 3 => IO.println("Goodbye!") // Option 3: End the application
        case _ =>
          IO.println(
            "Wrong input. Enter your choice (1,2 or 3)"
          ) *> menuLoop // Handle invalid input gracefully and loop back
      }
    yield ()
  }

  /** The application entry point.
    *
    * This starts the menu loop provided by the `menuLoop` function, allowing users to interact with
    * the application.
    *
    * @return
    *   An `IO[Unit]` effect that represents the application's main entry point.
    */
  override val run: IO[Unit] = menuLoop
