package org.jbbouille.supermonstar

import akka.actor.{Actor, ActorLogging}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

case class DirWalker(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  override def receive: Receive = ???
}
