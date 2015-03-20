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
  private val EXTENSION = "*.{mp3,wav,ogg}"
  private val pathMusicMatcher = FileSystems.getDefault().getPathMatcher(EXTENSION)

  private def checkExtension(path: Path): Boolean = {
    if(pathMusicMatcher.matches(path)) true
    else false
  }

  private def findPathOfMusic(path: Path): Unit = {
    Files.newDirectoryStream(path).foreach { file =>
      if (Files.isDirectory(file)) dirWalker ! Directory(file)
      else if (checkExtension(file)) musicMaker ! Track(file)
    }
  }

  def receive: Receive = {
    case Directory(path) => findPathOfMusic(path)
    case _ => log.warning("DirWalker shouldn't receive message {}", _)
  }
}
