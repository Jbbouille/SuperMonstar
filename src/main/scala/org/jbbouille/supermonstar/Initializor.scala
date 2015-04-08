package org.jbbouille.supermonstar

import java.nio.file.Paths
import scala.concurrent.duration._
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.indices.IndexMissingException
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import com.typesafe.config.Config
import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.routing.SmallestMailboxPool
import akka.util.Timeout
import scaldi.Module
import scaldi.akka.AkkaInjectable
import spray.can.Http

case class Initializor(config: Config, implicit val system: ActorSystem) extends Actor with AkkaInjectable with ActorLogging {

  val nbWorkersInPool = config.getInt("parameter.actor.nb.workers.in.pool")
  val directoryRoot = config.getString("parameter.directory.root")
  val interfaceServer = config.getString("parameter.interface")
  val portServer = config.getInt("parameter.port")
  val corsEnable = config.getBoolean("parameter.elastic.http.cors.enabled")
  val allowOriginUrl = config.getString("parameter.elastic.http.cors.allow-origin")
  val web = config.getString("parameter.web")

  val settings = ImmutableSettings.settingsBuilder()
    .put("http.cors.enabled", corsEnable)
    .put("http.cors.allow-origin", allowOriginUrl)
    .build()

  private val node = nodeBuilder()
    .settings(settings)
    .local(true)
    .data(true)
    .node()

  val clientEs = node.client()

  def receive: Receive = {
    case _ => log.warning("Initializor Actor shouldn't receive any message")
  }

  def elasticContainsElements(): Boolean = {
    try {
      val search = clientEs.prepareSearch("musicbank")
        .setTypes("music")
        .setQuery(QueryBuilders.wildcardQuery("uri", "*"))
        .execute()
        .actionGet()

      if (search.getHits.totalHits() > 0) true
      else false
    }
    catch {
      case e: IndexMissingException => false
    }
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()

    var dirWalker: ActorRef = null
    var elasticWriter: ActorRef = null
    var musicMaker: ActorRef = null
    var sprayRouter: ActorRef = null
    var elasticReader: ActorRef = null

    val actorModule = new Module {
      bind[ActorRef] identifiedBy required('dirWalker) to dirWalker
      bind[ActorRef] identifiedBy required('elasticWriter) to elasticWriter
      bind[ActorRef] identifiedBy required('musicMaker) to musicMaker
      bind[ActorRef] identifiedBy required('sprayRouter) to sprayRouter
      bind[ActorRef] identifiedBy required('elasticReader) to elasticReader
    }

    val configModule = new Module {
      bind[String] identifiedBy required('web) to web
      bind[Client] identifiedBy required('clientEs) to clientEs
    }

    val modules = actorModule :: configModule

    dirWalker = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[DirWalker], modules)), "dirWalker")
    elasticWriter = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[ElasticWriter], modules)), "elasticWriter")
    musicMaker = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[MusicMaker], modules)), "musicMaker")
    sprayRouter = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[SprayRouter], modules)), "sprayRouter")
    elasticReader = context.actorOf(SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[ElasticReader], modules)), "elasticReader")

    implicit val timeout = Timeout(5.seconds)
    IO(Http) ? Http.Bind(sprayRouter, interface = interfaceServer, port = portServer)

    if (!elasticContainsElements()) {
      log.warning("Searching music in {}", directoryRoot)
      dirWalker ! Directory(Paths.get(directoryRoot))
    }
  }
}
