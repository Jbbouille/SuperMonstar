package org.jbbouille.supermonstar

import java.nio.file.{FileSystems, Files, Path}
import scala.collection.JavaConversions._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object DirWalker {
  def props(implicit injector: Injector): Props = Props(new DirWalker)
}

case class DirWalker(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {

  private val musicMaker = inject[ActorRef]('musicMaker)
  private val dirWalker = inject[ActorRef]('dirWalker)
  private val EXTENSION = "glob:*.{mp3,wav,ogg,flac,aiff,wma}"
  private val pathMusicMatcher = FileSystems.getDefault().getPathMatcher(EXTENSION)

  private def checkExtension(path: Path): Boolean = {
    if (pathMusicMatcher.matches(path.getFileName)) true
    else false
  }

  private def findPathOfMusic(path: Path): Unit = {
    val newDirectoryStream = Files.newDirectoryStream(path)
    newDirectoryStream.foreach { file =>
      if (Files.isDirectory(file)) dirWalker ! Directory(file)
      else if (checkExtension(file)) musicMaker ! Link(file)
    }
    newDirectoryStream.close()
  }

  def receive: Receive = {
    case Directory(path) => findPathOfMusic(path)
    case _ => log.warning(s"DirWalker can receive only message of type Directory")
  }
}
