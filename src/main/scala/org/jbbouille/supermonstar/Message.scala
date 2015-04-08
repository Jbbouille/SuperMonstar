package org.jbbouille.supermonstar

import java.nio.file.Path

sealed trait Message

sealed trait Jsonable {
  def toJs: String
}

case class Directory(path: Path) extends Message
case class Link(path: Path) extends Message
case class Music(uri: Path, genre: String, artist: String, composer: String, album: String, track: String, date: String, title: String, length: String) extends Message with Jsonable {
  def toJs = s"""{"uri":"$uri","genre":"$genre","artist":"$artist","composer":"$composer","album":"$album","track":$track,"date":$date,"title":"$title","length":$length}"""

  def id = {
    val part1 = title.splitAt(title.length / 2)._1.replace(" ", "")
    val part2 = artist.splitAt(artist.length / 2)._1.replace(" ", "")
    val part3 = album.splitAt(album.length / 2)._1.replace(" ", "")
    s"${part1}_${part2}_${part3}_$date"
  }
}