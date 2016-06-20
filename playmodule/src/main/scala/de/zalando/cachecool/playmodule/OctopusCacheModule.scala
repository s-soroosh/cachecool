package de.zalando.cachecool.playmodule

import com.google.inject.name.Names
import com.typesafe.config.{ConfigObject, ConfigValue}
import de.zalando.cachecool.{CacheDefinition, OctopusCache, OctopusCacheFactory}
import play.api.{Configuration, Environment, Logger}
import play.api.inject.{Binding, Module}
import scala.collection.JavaConverters._


/**
  * de.zalando.cachecool.OctopusCacheModule is a play module that enables your application to use different caching definition in the same time.
  *
  * Below is the configuration example that creates a multi-level cache as default and a memory cache with name "local".
  *
  * @note To use non default caches the name have to be mentioned via @Named annotation.
  * @example
  * <pre>
  * octopus.cache.factories = {
  * guava: de.zalando.octopus.commons.cache.de.zalando.cachecool.GuavaCacheFactory
  * redis: de.zalando.octopus.commons.cache.de.zalando.cachecool.RedisCacheFactory
  * multi-level: de.zalando.octopus.commons.cache.de.zalando.cachecool.MultilevelCacheFactory
  * }
  *
  * octopus.caches = {
  * memory: {
  *  factory: "guava"
  *  config: {
  *    max-size: 10000
  *  }
  *  default: true
  *}
  * multi-level: {
  *  factory: "multi-level"
  *  config: {
  *    levels: [{
  *      factory: "guava"
  *      config: {
  *        max-size: 1000
  *        ttl: 10 minute
  *      }
  *    },
  *      {
  *        factory: "redis"
  *        config: {
  *          host: localhost
  *          port: 6379
  *          ttl: 30 minute
  *        }
  *      }]
  *  }
  *  default: false
  * }
  *}
  * </pre>
  * @author  Soroosh Sarabadani
  */
class OctopusCacheModule extends Module {

  private val logger = Logger(this.getClass.getName)


  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val factories = loadCacheFactories(environment, configuration)

    val octopusCacheConfig: ConfigObject = configuration.getObject("octopus.caches").getOrElse(throw new RuntimeException("Please provide cache configs"))
    val caches = octopusCacheConfig.asScala
    caches.flatMap { case (name, config) => buildCache(name, config, factories)
    }.toSeq
  }

  def buildCache(name: String, config: ConfigValue, factories: Map[String, OctopusCacheFactory]): Seq[Binding[_]] = {
    val cacheDef = CacheDefinition(config)
    val factory = factories.get(cacheDef.factoryName).getOrElse(throw new RuntimeException(s"No factory is defined for ${cacheDef.factoryName}"))

    val cache = factory.build(name, cacheDef.factoryConfig, factories)
    val default = cacheDef.isDefault match {
      case true => Some(bind(classOf[OctopusCache]).toInstance(cache))
      case _ => None
    }
    Seq(Some(bind(classOf[OctopusCache]).qualifiedWith(Names.named(name)).toInstance(cache)), default).flatten
  }


  private def loadCacheFactories(environment: Environment, configuration: Configuration): Map[String, OctopusCacheFactory] = {
    val factoriesDefinition = configuration.getObject("octopus.cache.factories").getOrElse(throw new RuntimeException("Please provide cache factories in config file")).asScala
    (factoriesDefinition map {
      case (name, className) => {
        val clazz: Class[_] = environment.classLoader.loadClass(className.unwrapped().toString)
        logger.info(s"Loading cache factory with name:$name and class:${clazz}")
        val constructor = clazz.getConstructor(classOf[ClassLoader])
        val factoryInstance: OctopusCacheFactory = constructor.newInstance(environment.classLoader).asInstanceOf[OctopusCacheFactory]
        (name -> factoryInstance)
      }
    }).toMap

  }
}
