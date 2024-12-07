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

  val apikey: fs2.Stream[IO, OpenAIConfig] = fs2.Stream.eval(loadOpenAIConfig().load[IO])
  val fileProcessor = FileProcessor.make[IO]()
  val inputPath = Path(
    "/Users/raphaelkhan/Developer/scalaai-doc/src/main/scala/io/github/khanr1/scalaaidoc/core"
  )
  val inputPathError = Path("/Users/raphaelkhan/Developer/scalaai-doc/src/test")
  // val run: IO[Unit] = apikey
  //   .flatMap(key => ScalaDocGenerator.make[IO](key.apiKey).generateReadMe(inputPath))
  //   .compile
  //   .drain
  val run: IO[Unit] =
    apikey
      .flatMap(key => ScalaDocGenerator.make[IO](key.apiKey).generateAllScalaDoc(inputPath))
      .compile
      .drain
