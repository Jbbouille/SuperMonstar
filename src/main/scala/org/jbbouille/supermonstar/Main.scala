package org.jbbouille.supermonstar

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}

object Main extends App {
  val system = ActorSystem("SupermONstar")
  val initializor = system.actorOf(Props(classOf[Initializor], ConfigFactory.load(), system), "initializor")
}
