package de.zalando.cachecool

import com.google.common.cache.CacheBuilder
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, _}
import scalacache.guava.GuavaCache
import de.zalando.cachecool.extensions._

/**
  * Created by ssarabadani on 07/06/16.
  */
class MultilevelCacheFactorySpec extends WordSpec with Matchers {

  val factory = new MultilevelCachecoolFactory(this.getClass.getClassLoader)

  val c1 = new FakeCache("Fake1", Some(1 minute))
  val c2 = new FakeCache("Fake2", Some(2 minute))
  c2.put("k1", "v1", None).result()

  private val cc: GenericCache = factory.build("TEST_CACHE", Seq(c1, c2))

  "Multilevel cache" should {
    "update front cache" in {
      cc.get("k1").result()
      assert(c1.get[String]("k1").result().get == "v1")

    }

    "update front cache when entry is expired" in {
      val gfc1 = new GuavaFakeCache("GFC1", Some(1 second))
      val gfc2 = new GuavaFakeCache("GFC2", Some(2 second))
      gfc1.put("k1", "v1", None).result()
      gfc2.put("k1", "v2", None).result()
      val gfcc = factory.build("GFC_M_CACHE", Seq(gfc1, gfc2))

      assert(gfcc.get("k1").result() == Some("v1"))
      Thread.sleep(1002l)
      assert(gfcc.get("k1").result() == Some("v2"))

    }
  }

}

class FakeCache(override val name: String, override val defaultTTL: Option[Duration] = None) extends GenericCache {

  val cache = mutable.Map[String, Object]()


  override protected def _get[A](key: String): Future[Option[A]] = Future.successful(cache.get(key).map(_.asInstanceOf[A]))

  override protected def _remove(key: String): Future[Unit] = Future.successful(cache.remove(key))

  override protected def _put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit] = Future.successful(cache.put(key, obj.asInstanceOf[Object]))

}

class GuavaFakeCache(override val name: String, override val defaultTTL: Option[Duration] = None) extends GenericCache {

  val cache = GuavaCache(CacheBuilder.newBuilder().maximumSize(10).build[String, Object])

  override protected def _get[A](key: String): Future[Option[A]] = cache.get(key)

  override protected def _put[A](key: String, obj: A, duration: Option[Duration]): Future[Unit] = cache.put(key, obj, defaultTTL)

  override protected def _remove(key: String): Future[Unit] = cache.remove(key)
}
