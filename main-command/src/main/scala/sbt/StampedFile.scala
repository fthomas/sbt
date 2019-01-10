/*
 * sbt
 * Copyright 2011 - 2018, Lightbend, Inc.
 * Copyright 2008 - 2010, Mark Harrah
 * Licensed under Apache License 2.0 (see LICENSE)
 */

package sbt

import java.io.File
import java.nio.file.Path

import sbt.internal.inc.Stamper
import sbt.io.TypedPath
import xsbti.compile.analysis.Stamp

/**
 * A File that has a [[Stamp]] value associated with it. In general, the stamp method should be
 * a cached value that can be read without doing any io. This can be used to improve performance
 * anywhere where we need to check if files have changed before doing potentially expensive work.
 */
trait StampedFile extends File with TypedPath {
  def stamp: Stamp
}

/**
 * Provides converter functions from TypedPath to [[StampedFile]].
 */
object StampedFile {

  /**
   * Converts a TypedPath instance to a [[StampedFile]] by calculating the file hash.
   */
  val sourceConverter: TypedPath => StampedFile =
    new StampedFileImpl(_: TypedPath, forceLastModified = false)

  /**
   * Converts a TypedPath instance to a [[StampedFile]] using the last modified time.
   */
  val binaryConverter: TypedPath => StampedFile =
    new StampedFileImpl(_: TypedPath, forceLastModified = true)

  /**
   * A combined convert that converts TypedPath instances representing *.jar and *.class files
   * using the last modified time and all other files using the file hash.
   */
  val converter: TypedPath => StampedFile = (tp: TypedPath) =>
    tp.toPath.toString match {
      case s if s.endsWith(".jar")   => binaryConverter(tp)
      case s if s.endsWith(".class") => binaryConverter(tp)
      case _                         => sourceConverter(tp)
  }

  /**
   * Adds a default ordering that just delegates to the java.io.File.compareTo method.
   */
  implicit case object ordering extends Ordering[StampedFile] {
    override def compare(left: StampedFile, right: StampedFile): Int = left.compareTo(right)
  }

  private class StampedFileImpl(typedPath: TypedPath, forceLastModified: Boolean)
      extends java.io.File(typedPath.toPath.toString)
      with StampedFile {
    override val stamp: Stamp =
      if (forceLastModified || typedPath.isDirectory) Stamper.forLastModified(this)
      else Stamper.forHash(this)
    override def exists: Boolean = typedPath.exists
    override def isDirectory: Boolean = typedPath.isDirectory
    override def isFile: Boolean = typedPath.isFile
    override def isSymbolicLink: Boolean = typedPath.isSymbolicLink
    override def toPath: Path = typedPath.toPath
  }
}
