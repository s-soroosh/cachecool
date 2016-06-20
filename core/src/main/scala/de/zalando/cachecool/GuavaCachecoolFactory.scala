package de.zalando.cachecool

import com.google.common.cache.CacheBuilder
import com.typesafe.config.ConfigValue

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by ssarabadani on 03/06/16.
  */
class GuavaCachecoolFactory(classLoader: ClassLoader) extends CachecoolFactory(classLoader) {

  override def build(cacheName: String, config: ConfigValue, factories: Map[String, CachecoolFactory]): GenericCache = {

    val c = config.atKey("c")
    val maxSize = c.getLong("c.max-size")
    val ttl = readTTL(c)

    val underlyingGuavaCache = CacheBuilder.newBuilder().maximumSize(maxSize).build[String, Object]
    val cache = scalacache.guava.GuavaCache(underlyingGuavaCache)

    new GenericCache {
      override val name: String = cacheName

      override val defaultTTL: Option[Duration] = ttl

      override def _get[A](key: String): Future[Option[A]] = cache.get(key)

      override def _put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit] = cache.put(key, obj, duration)

      override def _remove(key: String): Future[Unit] = cache.remove(key)

    }
  }
}

