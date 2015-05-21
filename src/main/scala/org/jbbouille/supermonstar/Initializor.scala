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

  private var dirWalker: ActorRef = null
  private var elasticWriter: ActorRef = null
  private var musicMaker: ActorRef = null
  private var sprayRouter: ActorRef = null
  private var elasticReader: ActorRef = null

  private val nbWorkersInPool = config.getInt("parameter.actor.nb.workers.in.pool")
  private val directoryRoot = config.getString("parameter.directory.root")
  private val interfaceServer = config.getString("parameter.interface")
  private val portServer = config.getInt("parameter.port")
  private val corsEnable = config.getBoolean("parameter.elastic.http.cors.enabled")
  private val allowOriginUrl = config.getString("parameter.elastic.http.cors.allow-origin")
  private val web = config.getString("parameter.web")

  private val settings = ImmutableSettings.settingsBuilder()
    .put("http.cors.enabled", corsEnable)
    .put("http.cors.allow-origin", allowOriginUrl)
    .build()

  private val node = nodeBuilder()
    .settings(settings)
    .local(true)
    .data(true)
    .node()

  private val clientEs = node.client()

  private def searchMusic(): Unit = {
    log.warning("Searching music in {}", directoryRoot)
    dirWalker ! Directory(Paths.get(directoryRoot))
  }

  private def elasticContainsElements(): Boolean = {
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

  def receive: Receive = {
    case Search => searchMusic()
    case _ => log.warning("Initializor Actor shouldn't receive any message")
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()

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

    def props[T] = {
      SmallestMailboxPool(nbWorkersInPool).props(Props(classOf[T], modules))
    }

    dirWalker = context.actorOf(props[DirWalker], "dirWalker")
    elasticWriter = context.actorOf(props[ElasticWriter], "elasticWriter")
    musicMaker = context.actorOf(props[MusicMaker], "musicMaker")
    sprayRouter = context.actorOf(props[SprayRouter], "sprayRouter")
    elasticReader = context.actorOf(props[ElasticReader], "elasticReader")

    implicit val timeout = Timeout(5.seconds)
    IO(Http) ? Http.Bind(sprayRouter, interface = interfaceServer, port = portServer)

    if (!elasticContainsElements()) searchMusic()
  }
}
