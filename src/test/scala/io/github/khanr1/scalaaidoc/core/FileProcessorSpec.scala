package io.github.khanr1.scalaaidoc.core

import weaver.*
import fs2.io.file.Path
import cats.effect.IO
import cats.effect.unsafe.implicits.global

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.testing.TestingLogger

object FileProcessorSpec extends SimpleIOSuite:
  given Logger[IO] = TestingLogger.impl[IO]()
  val pathToDirectory = Path(
    "/Users/raphaelkhan/Developer/scalaai-doc/src/test/resources/"
  )
  val pathToGoodFile = Path(
    "/Users/raphaelkhan/Developer/scalaai-doc/src/test/resources/Test.scala"
  )
  val pathToWrongFile = Path("/Users/raphaelkhan/Developer/scalaai-doc/src/test/resources/Test.txt")
  val mockContent = FileContent("val test = \"test\"\n")

  test("readScalaFile should return file content for a valid .scala file") {
    val fileProcessor = FileProcessor.make[IO]()
    val result = fileProcessor.readScalaFile(path = pathToGoodFile).compile.toList
    for contents <- result
    yield expect(contents.map(_.value.trim()) == List(mockContent.value.trim()))

  }
  test("readScalaFile should return error InvalidPathError when file is not a  .scala file") {
    val fileProcessor = FileProcessor.make[IO]()
    val result = fileProcessor.readScalaFile(path = pathToWrongFile).compile.drain.attempt
    for contents <- result
    yield expect(
      contents == Left(
        FileProcessorError.InvalidPathError(
          s"The path does not lead to a scala file: $pathToWrongFile"
        )
      )
    )

  }

  test("readAllScala file should return the content of all the scala file in a directory") {
    val fileProcessor = FileProcessor.make[IO]()
    val result = fileProcessor.readAllScalaFiles(pathToDirectory).compile.toList
    for contents <- result
    yield expect(contents.length == 2)

  }
