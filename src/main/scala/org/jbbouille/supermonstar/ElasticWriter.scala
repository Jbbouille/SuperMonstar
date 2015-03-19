package org.jbbouille.supermonstar

import akka.actor.{ActorLogging, Actor}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

case class ElasticWriter(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  override def receive: Receive = ???
}
