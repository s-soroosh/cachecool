package de.zalando.cachecool

import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits
import scala.util.Try


trait CachingSupport {

  def cacheName: String = "Default"

  /**
    * @note
    * This value will be ignored if a multi-level cache implementation is used.
    */
  val defaultTtl = Duration(Try(ConfigFactory.load().getDuration(s"cache.${cacheName}.ttl")).getOrElse(java.time.Duration.ZERO).toMillis, TimeUnit.MILLISECONDS)

  private val enabled = defaultTtl != Duration.Zero

  @Inject(optional = true)
  val usedCache: GenericCache = TransientCache

  def get[A](key: String): Future[Option[A]] =
    if (enabled) {
      usedCache.get[A](cacheName + ":" + key)
    } else {
      Future.successful(None)
    }

  def put[A](key: String, obj: A, duration: Duration = defaultTtl): Future[Unit] =
    if (enabled) {
      usedCache.put[A](cacheName + ":" + key, obj, Option(duration))
    } else {
      Future.successful(())
    }


  def removeFromCache(key: String): Future[Unit] =
    if (enabled) {
      usedCache.remove(cacheName + ":" + key)
    } else {
      Future.successful(())
    }


  def getOrElse[A](key: String, duration: Duration = defaultTtl)(orElse: => Future[Option[A]])(implicit executionContext: ExecutionContext = Implicits.global): Future[Option[A]] = {
    get[A](key).flatMap(itemOption => {
      itemOption.fold(
        // empty - cache miss
        orElse.map(dbItem =>
          dbItem.fold(
            // no result present
            Option.empty[A]
          )(
            s => {
              // result found -> store in cache and return it
              put[A](key, s, duration)
              Option(s)
            }
          )
        )
      )(
        // cache hit
        x => Future.successful(Option(x)))
    })
  }
}
