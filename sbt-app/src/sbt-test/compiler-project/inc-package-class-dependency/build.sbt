import sbt.internal.inc.Analysis

TaskKey[Unit]("verify-binary-deps") := {
  val a = (Compile / compile).value match { case a: Analysis => a }
  val classDir = (Compile / classDirectory).value
  val base = baseDirectory.value
  val nestedPkgClass = classDir / "test/nested.class"
  val fooSrc = base / "src/main/scala/test/nested/Foo.scala"
  val converter = fileConverter.value
  assert(!a.relations.libraryDeps(converter.toVirtualFile(fooSrc.toPath))
    .contains(converter.toVirtualFile(nestedPkgClass.toPath)), a.relations.toString)
}
