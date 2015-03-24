package org.jbbouille.supermonstar

import java.nio.file.Path

sealed trait Message

case class Directory(path: Path) extends Message
case class Link(path: Path) extends Message
case class Music(uri: Path,
                 genre: Option[String], 
                 creator: Option[String], 
                 album: Option[String], 
                 track: Option[String], 
                 releaseDate: Option[String], 
                 title: Option[String]) extends Message