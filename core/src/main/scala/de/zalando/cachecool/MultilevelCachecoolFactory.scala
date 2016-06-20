package de.zalando.cachecool

import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.config.{ConfigObject, ConfigValue}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * Created by ssarabadani on 06/06/16.
  */
class MultilevelCachecoolFactory(classLoader: ClassLoader) extends CachecoolFactory(classLoader) {


  override def build(cacheName: String, config: ConfigValue, factories: Map[String, CachecoolFactory]): GenericCache = {
    val c = config.atKey("c")
    val levels: mutable.Buffer[_ <: ConfigObject] = c.getObjectList("c.levels").asScala

    val nestedCaches: mutable.Buffer[GenericCache] = levels.map { config =>
      val factoryName = config.atKey("c").getString("c.factory")
      val cacheConfig = config.atKey("c").getObject("c.config")
      factories.get(factoryName).map(_.build(s"multi-level-$factoryName", cacheConfig, factories)).getOrElse(throw new RuntimeException(s"No factory with name:${factoryName} is provided"))
    }

    build(cacheName, nestedCaches)
  }

  protected[cachecool] def build(cacheName: String, nestedCaches: Seq[GenericCache]): GenericCache = {
    new GenericCache {
      override val name: String = cacheName

      override val defaultTTL: Option[Duration] = None

      override def _get[A](key: String): Future[Option[A]] = findFirst(nestedCaches, key)

      override def _put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit] = doOnAll(nestedCaches, c => c.put(key, obj, None))

      override def _remove(key: String): Future[Unit] = doOnAll(nestedCaches, c => c.remove(key))

    }
  }


  private def findFirst[A](caches: Seq[GenericCache], key: String, prevCaches: Seq[GenericCache] = Seq()): Future[Option[A]] = {
    caches.length match {
      case 0 => Future.successful(None)
      case _ => {
        val result: Future[Option[A]] = caches.head.get(key).filter(_ != None).recoverWith {
          case _ => findFirst(caches.tail, key, prevCaches :+ caches.head)
        }
        result.flatMap {
          case Some(v) => {
            doOnAll(prevCaches, c => c.put(key, v, None)).map(u => Some(v))
          }
          case a => Future.successful(a)
        }
      }
    }
  }

  private def doOnAll[A](caches: Seq[GenericCache], what: GenericCache => Future[Unit]): Future[Unit] = {
    val result = Promise[Unit]
    if (caches.length == 0) result tryComplete (Success(()))
    val remaining = new AtomicInteger(caches.length)

    caches.map(what(_)).foreach {
      _ onComplete {
        case s@Success(_) => {
          if (remaining.decrementAndGet() == 0) {
            result tryComplete s
          }
        }
        case f@Failure(_) => {
          result tryComplete f
        }
      }

    }
    result.future
  }

}

