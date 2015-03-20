package org.jbbouille.supermonstar

import java.nio.file.Path

sealed trait Message

case class Directory(path: Path) extends Message
case class Track(path: Path) extends Message
