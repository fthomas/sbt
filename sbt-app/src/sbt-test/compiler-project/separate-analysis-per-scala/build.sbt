lazy val scala212 = "2.12.12"
lazy val scala213 = "2.13.1"
ThisBuild / scalaVersion := scala212

lazy val root = (project in file("."))
  .settings(
    name := "foo",
    crossScalaVersions := List(scala212, scala213),
    incOptions := incOptions.value.withClassfileManagerType(
      Option(xsbti.compile.TransactionalManagerType.of(
        crossTarget.value / "classes.bak",
        (Compile / compile / streams).value.log
      ): xsbti.compile.ClassFileManagerType).asJava
    )
  )
