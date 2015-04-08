package org.jbbouille.supermonstar

import java.nio.file.Paths
import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import com.typesafe.config.Config
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.SmallestMailboxPool
import scaldi.Module
import scaldi.akka.AkkaInjectable

case class Initializor(config: Config) extends Actor with AkkaInjectable with ActorLogging {

  val nbWorkersInPool = config.getInt("parameter.actor.nbWorkersInPool")
  val directoryRoot = config.getString("parameter.directoryRoot")

  private val node = nodeBuilder().local(true).data(true).node()
  val clientEs = node.client()

  def receive: Receive = {
    case _ => log.warning("Initializor Actor shouldn't receive any message")
  }

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
      bind[Client] identifiedBy required('clientEs) to clientEs
    }

    dirWalker = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[DirWalker], actorModule)), "dirWalker")
    elasticWriter = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[ElasticWriter], actorModule)), "elasticWriter")
    musicMaker = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[MusicMaker], actorModule)), "musicMaker")
    sprayRouter = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[SprayRouter], actorModule)), "sprayRouter")
    elasticReader = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[ElasticReader], actorModule)), "elasticReader")

    dirWalker ! Directory(Paths.get(directoryRoot))

  }
}
