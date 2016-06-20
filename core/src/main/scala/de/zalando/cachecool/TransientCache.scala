package de.zalando.cachecool

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by nscherer on 21/01/16.
  */
object TransientCache extends OctopusCache {

  override val name: String = "Transient"

  override val defaultTTL: Option[Duration] = None

  override def _get[A](key: String): Future[Option[A]] = Future.successful(None)

  override def _put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit] = Future.successful(())

  override def _removeFromCache(key: String): Future[Unit] = Future.successful(())

}
