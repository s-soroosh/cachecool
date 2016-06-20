package de.zalando.cachecool

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by nscherer on 21/01/16.
  */

trait Cache {

  def get[A](key: String): Future[Option[A]]

  def put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit]

  def remove(key: String): Future[Unit]
}
