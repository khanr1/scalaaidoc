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

object Main extends IOApp.Simple:
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  val run: IO[Unit] = {
    val apikey: fs2.Stream[IO, OpenAIConfig] = fs2.Stream.eval(loadOpenAIConfig().load[IO])
    val fileProcessor = FileProcessor.make[IO]()
    val inputPath = Path(
      "/Users/raphaelkhan/Developer/scalaai-doc/src/main/scala/io/github/khanr1/scalaaidoc/core"
    )
    val inputPathError = Path("/Users/raphaelkhan/Developer/scalaai-doc/src/test")
    apikey.flatMap { key =>
      val scalaDocGenerator = core.ScalaDocGenerator.make[IO](key.apiKey) // Initialize once
      fs2.Stream.eval(Logger[IO].info("Starting Program")) *>
        fileProcessor
          .readAllScalaFiles(inputPath) // Stream of (Path, FileContent)
          .flatMap { case (path, content) =>
            // Generate ScalaDoc for each file
            scalaDocGenerator.generateScalaDoc(path)
          }
          .handleErrorWith { e =>
            fs2.Stream.eval(IO.println(s"Error: ${e.toString()}"))
          } // Log or print the result
    }
  }.compile.drain
