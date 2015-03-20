package org.jbbouille.supermonstar

import akka.actor.{Props, ActorLogging, Actor}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object MusicMaker {
  def props(implicit injector: Injector): Props = Props(new MusicMaker)
}

case class MusicMaker(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  def receive: Receive = {
    case "hello" => println("hello")
  }
}
