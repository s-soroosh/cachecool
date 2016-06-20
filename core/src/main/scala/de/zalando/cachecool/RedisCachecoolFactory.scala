package de.zalando.cachecool

import com.typesafe.config.ConfigValue
import redis.clients.jedis.JedisPool

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by ssarabadani on 06/06/16.
  */
class RedisCachecoolFactory(classLoader: ClassLoader) extends CachecoolFactory(classLoader: ClassLoader) {


  override def build(cacheName: String, config: ConfigValue, factories: Map[String, CachecoolFactory]): GenericCache = {
    val c = config.atKey("c")
    val host = c.getString("c.host")
    val port = c.getInt("c.port")
    val ttl = readTTL(c)

    val pool = new JedisPool(host, port)
    val cache = scalacache.redis.RedisCache(pool, Option(classLoader))

    return new GenericCache {
      override val name: String = cacheName

      override val defaultTTL: Option[Duration] = ttl

      override def _get[A](key: String): Future[Option[A]] = cache.get(key)

      override def _put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit] = cache.put(key, obj, duration)

      override def _remove(key: String): Future[Unit] = cache.remove(key)
    }

  }
}
