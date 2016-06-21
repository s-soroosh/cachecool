package de.zalando.cachecool

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

/**
  * Created by nscherer on 21/01/16.
  */

trait Cache {

  def get[A](key: String): Future[Option[A]]

  def put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit]

  def remove(key: String): Future[Unit]

  def getOrElse[A](key: String, duration: Option[Duration] = None)(orElse: => Future[Option[A]])(implicit executionContext: ExecutionContext = Implicits.global): Future[Option[A]] = {
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
