package org.jbbouille.supermonstar

import java.nio.file.Path
import java.util.logging.{Level, Logger}
import org.jaudiotagger.audio.{AudioFileIO, AudioHeader}
import org.jaudiotagger.tag.{FieldKey, Tag}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object MusicMaker {
  def props(implicit injector: Injector): Props = Props(new MusicMaker)
}

case class MusicMaker(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  val elasticWriter = inject[ActorRef]('elasticWriter)

  def receive: Receive = {
    case Link(path) => createMusic(path)
    case _ => log.warning(s"MusicMaker can receive only message of type Link")
  }

  def createMusic(path: Path): Unit = {
    val f = AudioFileIO.read(path.toFile)
    elasticWriter ! toMusic(f.getTag(), f.getAudioHeader(), path)
  }

  def toMusic(tag: Tag, audioHeader: AudioHeader, path: Path): Music = {
    new Music(path,
      tag.getFirst(FieldKey.GENRE),
      tag.getFirst(FieldKey.ARTIST),
      tag.getFirst(FieldKey.COMPOSER),
      tag.getFirst(FieldKey.ALBUM),
      tag.getFirst(FieldKey.TRACK),
      tag.getFirst(FieldKey.YEAR),
      tag.getFirst(FieldKey.TITLE),
      audioHeader.getTrackLength.toString
    )
  }
}
