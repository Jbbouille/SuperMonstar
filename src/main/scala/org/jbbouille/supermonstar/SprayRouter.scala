package org.jbbouille.supermonstar

import akka.actor.{ActorLogging, Actor}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

class SprayRouter(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  override def receive: Receive = ???
}
