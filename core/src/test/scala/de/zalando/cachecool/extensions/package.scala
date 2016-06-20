package de.zalando.cachecool

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * Created by ssarabadani on 20/06/16.
  */
package object extensions {
  implicit def Future2WaitableFuture[T](self: Future[T]) = new WaitableFuture[T](self)

  class WaitableFuture[+T] (future: Future[T]) {
    /**
      * by importing Extensions, result method will be implicitly added to Future objects.
      * Rather than using Await.result... you would be able to call futureObject.result() which is more convenient.
      *
      * @note If you decided to use this method outside of the tests then think again.
      * @param duration
      * @return future result
      */
    def result(duration: Duration = Duration.Inf): T = Await.result(future, duration)
  }
}
