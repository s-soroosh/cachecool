package de.zalando.cachecool

import com.typesafe.config.{Config, ConfigValue}

import scala.concurrent.duration.Duration

/**
  * Created by ssarabadani on 03/06/16.
  */
abstract class CachecoolFactory(classLoader: ClassLoader) {

  def build(name: String, config: ConfigValue, factories: Map[String, CachecoolFactory]): GenericCache


  def readTTL(c: Config): Option[Duration] = {
    val ttl = c.hasPath("c.ttl") match {
      case true => Option(Duration.fromNanos(c.getDuration("c.ttl").toNanos))
      case _ => None
    }
    ttl
  }

}
