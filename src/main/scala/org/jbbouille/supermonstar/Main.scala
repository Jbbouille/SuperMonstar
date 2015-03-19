package org.jbbouille.supermonstar

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorRef, ActorSystem}
import scaldi.Module
import scaldi.akka.AkkaInjectable

class MainModule extends Module {
  bind[ActorSystem] to ActorSystem("Supermonstar2") destroyWith (_.shutdown())
}

class ModuleActors extends Module {
  bind[DirWalker] to new DirWalker
  bind[ElasticReader] to new ElasticReader
  bind[ElasticWriter] to new ElasticWriter
  bind[SprayRouter] to new SprayRouter
  bind[MusicMaker] to new MusicMaker

  //Singleton binding
  binding identifiedBy 'walker to {
    implicit val system = inject[ActorSystem]
    AkkaInjectable.injectActorRef[DirWalker]
  }

  binding identifiedBy 'esreader to {
    implicit val system = inject[ActorSystem]
    AkkaInjectable.injectActorRef[ElasticReader]
  }

  binding identifiedBy 'eswriter to {
    implicit val system = inject[ActorSystem]
    AkkaInjectable.injectActorRef[ElasticWriter]
  }

  binding identifiedBy 'spray to {
    implicit val system = inject[ActorSystem]
    AkkaInjectable.injectActorRef[SprayRouter]
  }

  binding identifiedBy 'music to {
    implicit val system = inject[ActorSystem]
    AkkaInjectable.injectActorRef[MusicMaker]
  }
}

class ModuleOther extends Module {
  binding to Initializor(ConfigFactory.load())
}

object Main extends App with AkkaInjectable {
  implicit val appModule = new ModuleActors :: new MainModule :: new ModuleOther
  implicit val system = inject[ActorSystem]
  inject[Initializor]
  private val waker = inject[ActorRef]('walker)
}
