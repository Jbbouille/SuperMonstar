package org.jbbouille.supermonstar

import org.elasticsearch.client.Client
import akka.actor.{Actor, ActorLogging, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object ElasticWriter {
  def props(implicit injector: Injector): Props = Props(new ElasticWriter)
}

case class ElasticWriter(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {

  val clientEs = inject[Client]('clientEs)

  def insertInElastic(music: Jsonable): Unit = {
    clientEs.prepareIndex("musicbank", "music", music.id).setSource(music.toJs).execute()
  }

  def receive: Receive = {
    case m: Music => insertInElastic(m)
  }
}
