package org.jbbouille.supermonstar

import akka.actor.{Props, ActorLogging, Actor}
import scaldi.{Injectable, Injector}
import scaldi.akka.AkkaInjectable

object SprayRouter extends Injectable{
  def props(implicit injector: Injector): Props = Props(new SprayRouter)
}

class SprayRouter(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  def receive: Receive = {
    case "" => println()
  }
}
