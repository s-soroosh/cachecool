package de.zalando.cachecool

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by ssarabadani on 03/06/16.
  */
abstract class GenericCache extends Cache with LoggingSupport {

  val name: String

  val defaultTTL: Option[Duration]

  protected def _get[A](key: String): Future[Option[A]]

  protected def _put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit]

  protected def _remove(key: String): Future[Unit]

  final override def get[A](key: String): Future[Option[A]] = {
    logger.debug(s"Getting $key from cache:$name")
    val result: Future[Option[Nothing]] = this._get(key)
    result onFailure { case t => logger.error(s"Error while getting $key from cache $name: ${t.getMessage}", t.getCause) }
    result
  }

  final override def put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit] = {
    val d = duration.orElse(defaultTTL)
    logger.debug(s"PUT key:$key obj:$obj duration:$d into cache:$name")
    val result = this._put(key, obj, d)
    result onFailure { case t => logger.error(s"Error while putting $key into cache $name: ${t.getMessage}", t.getCause) }
    result
  }

  final override def remove(key: String): Future[Unit] = {
    logger.debug(s"Removing key:$key from cache:$name")
    val result = this._remove(key)
    result onFailure { case t => logger.error(s"Error while removing $key from cache $name: ${t.getMessage}", t.getCause) }
    result
  }

  override def toString(): String = s"Cache:$name"
}
