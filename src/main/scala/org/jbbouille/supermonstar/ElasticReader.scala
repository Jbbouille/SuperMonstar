package org.jbbouille.supermonstar

import akka.actor.{Actor, ActorLogging, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object ElasticReader {
  def props(implicit injector: Injector): Props = Props(new ElasticReader)
}

case class ElasticReader(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  def receive: Receive = {
    case "" => println()
  }
}
