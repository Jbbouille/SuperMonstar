package org.jbbouille.supermonstar

import com.typesafe.config.Config

case class Initializor(config: Config) {

  val nbWorkersInPool = config.getInt("parameter.actor.nbWorkersInPool")

}
