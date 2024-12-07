import Dependencies.*

ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "io.github.khanr1"

lazy val `scalaai-doc` =
  project
    .in(file("."))
    .settings(name := "ScalaAI-Doc")
    .settings(dependencies)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    Libraries.catsEffect.value,
    Libraries.cats.value,
    Libraries.monocle.value,
    Libraries.kitten.value,
    Libraries.fs2.value,
    Libraries.fs2IO.value,
    Libraries.scalaopenai,
    Libraries.log4cats,
    Libraries.log4catslf4j,
    Libraries.log4catTest,
    "ch.qos.logback" % "logback-classic" % "1.4.11"
  ),
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  libraryDependencies ++= Seq(
    Libraries.weaverCats,
    Libraries.weaverDiscipline,
    Libraries.weaverScalaCheck,
    Libraries.scalaMock
  ).map(_ % Test)
)
