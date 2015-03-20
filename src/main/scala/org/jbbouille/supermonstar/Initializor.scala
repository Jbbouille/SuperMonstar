package org.jbbouille.supermonstar

import com.typesafe.config.Config
import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.routing.SmallestMailboxPool
import scaldi.Module
import scaldi.akka.AkkaInjectable

case class Initializor(config: Config) extends Actor with AkkaInjectable {

  val log = Logging(context.system, Initializor)

  def receive: Receive = {
    case _ => log.warning("Initializor Actor shouldn't receive any message even {}", _)
  }

  val nbWorkersInPool = config.getInt("parameter.actor.nbWorkersInPool")

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()

    var dirWalker: ActorRef = null
    var elasticWriter: ActorRef = null
    var musicMaker: ActorRef = null
    var sprayRouter: ActorRef = null
    var elasticReader: ActorRef = null

    implicit var actorModule = new Module {
      bind[ActorRef] identifiedBy required('dirWalker) to dirWalker
      bind[ActorRef] identifiedBy required('elasticWriter) to elasticWriter
      bind[ActorRef] identifiedBy required('musicMaker) to musicMaker
      bind[ActorRef] identifiedBy required('sprayRouter) to sprayRouter
      bind[ActorRef] identifiedBy required('elasticReader) to elasticReader
    }

    dirWalker = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[DirWalker], actorModule)), "dirWalker")
    elasticWriter = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[ElasticWriter], actorModule)), "elasticWriter")
    musicMaker = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[MusicMaker], actorModule)), "musicMaker")
    sprayRouter = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[SprayRouter], actorModule)), "sprayRouter")
    elasticReader = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[ElasticReader], actorModule)), "elasticReader")
  }
}
