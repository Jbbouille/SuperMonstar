package org.jbbouille.supermonstar

import akka.actor.{Props, ActorLogging, Actor}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object ElasticWriter {
  def props(implicit injector: Injector): Props = Props(new ElasticWriter)
}

case class ElasticWriter(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  def receive: Receive = {
    case "" => println()
  }
}
