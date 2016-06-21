package de.zalando.cachecool.playmodule

import javax.inject.{Inject, Singleton}

import de.zalando.cachecool.Cache
import play.api.cache.CacheApi

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag

/**
  * Created by ssarabadani on 21/06/16.
  */
@Singleton
class CachecoolApi @Inject()(val namespace: String, timeout: Duration, cache: Cache) extends CacheApi {
  override def set(key: String, value: Any, expiration: Duration): Unit = Await.ready(cache.put(key, value, Option(expiration)), timeout)

  override def get[T: ClassTag](key: String): Option[T] = Await.result(cache.get[T](key), timeout)

  def getOrElse[A: ClassTag](key: String, expiration: Duration = Duration.Inf)(orElse: => A): A = {
    val result: A = orElse
    Await.result(cache.getOrElse[A](key, Option(expiration))(Future {
      Option(result)
    }), timeout)
    result
  }

  override def remove(key: String): Unit = Await.ready(cache.remove(key), timeout)
}
