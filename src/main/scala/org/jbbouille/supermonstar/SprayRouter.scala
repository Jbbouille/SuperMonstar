package org.jbbouille.supermonstar

import akka.actor.{Actor, ActorLogging, Props}
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import spray.routing.{HttpService, Route}

object SprayRouter extends Injectable {
  def props(implicit injector: Injector): Props = Props(new SprayRouter)
}

class SprayRouter(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable with HttpService {

  val web = inject[String]('web)

  val myRoute = if (web.isEmpty) serveMusic else serveMusic ~ serveWebSite

  def serveMusic: Route = {
    path("music" / Segment) { uri =>
      getFromDirectory(uri)
    }
  }

  def serveWebSite: Route = {
    path("") {
      getFromDirectory(web)
    }
  }

  def actorRefFactory = context

  def receive: Receive = runRoute(myRoute)
}
