package org.jbbouille.supermonstar

import java.io.FileInputStream
import java.nio.file.{Files, Path}
import org.apache.tika.config.TikaConfig
import org.apache.tika.detect.Detector
import org.apache.tika.io.TikaInputStream
import org.apache.tika.io.TikaInputStream._
import org.apache.tika.metadata.{Metadata, TikaCoreProperties}
import org.apache.tika.parser.{AutoDetectParser, ParseContext}
import org.apache.tika.sax.BodyContentHandler
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object MusicMaker {
  def props(implicit injector: Injector): Props = Props(new MusicMaker)
}

case class MusicMaker(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {

  val elasticWriter = inject[ActorRef]('elasticWriter)

  def receive: Receive = {
    case Link(path) => createMusic(path)
    case _ => log.warning(s"MusicMaker can receive only message of type Link")
  }

  def createMusic(path: Path): Unit = {
    val tika = new TikaConfig()
    val detector: Detector = tika.getDetector
    val fileStream = TikaInputStream.get(Files.newInputStream(path))
    if (isAudioType(fileStream, path, detector)) {
      val metadata = extractMetaData(fileStream, path, detector)
      elasticWriter ! toMusic(metadata, path)
    }
  }

  def extractMetaData(tikaInputStream: TikaInputStream, path: Path, detector: Detector): Metadata = {
    val parser = new AutoDetectParser()
    val metadata = new Metadata()
    parser.parse(tikaInputStream, new BodyContentHandler(), metadata, new ParseContext())
    tikaInputStream.close()
    metadata
  }

  def isAudioType(fileStream: TikaInputStream, path: Path, detector: Detector): Boolean = {
    val metadata = new Metadata()
    metadata.add(TikaCoreProperties.TYPE, path.toString)
    detector.detect(get(new FileInputStream(path.toFile)), metadata).getType.equals("audio")
  }

  def toMusic(metadata: Metadata, path: Path): Music = {
    Music(path,
      optionizeMetadata(metadata.get("xmpDM:genre")),
      optionizeMetadata(metadata.get("creator")),
      optionizeMetadata(metadata.get("xmpDM:album")),
      optionizeMetadata(metadata.get("xmpDM:trackNumber")),
      optionizeMetadata(metadata.get("xmpDM:releaseDate")),
      optionizeMetadata(metadata.get("dc:title"))
    )
  }

  def optionizeMetadata(metadata: String): Option[String] =
    Option(metadata).filter(_.trim.nonEmpty)
}
