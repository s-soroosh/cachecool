package de.zalando.cachecool

import com.google.inject.name.Names
import org.scalatestplus.play.PlaySpec
import de.zalando.cachecool.extensions._
import play.api.inject.{BindingKey, Injector, QualifierInstance}

import scala.language.postfixOps
import scala.concurrent.duration._


/**
  * Created by ssarabadani on 03/06/16.
  */


class OctopusCacheModuleSpec extends PlaySpec with AcceptanceTestSuite {
  var memoryCache: OctopusCache = null
  var centralCache: OctopusCache = null
  var multilevelCache: OctopusCache = null

  override def inject(injector: Injector): Unit = {
    memoryCache = injector.instanceOf(BindingKey(classOf[OctopusCache], Some(QualifierInstance(Names.named("memory")))))
    centralCache = injector.instanceOf(BindingKey(classOf[OctopusCache], Some(QualifierInstance(Names.named("central")))))
    multilevelCache = injector.instanceOf(BindingKey(classOf[OctopusCache], Some(QualifierInstance(Names.named("multi-level")))))
  }

  "this test" should {
    "be passed" in {
      assert(memoryCache != null)
      assert(centralCache != null)
      assert(multilevelCache != null)
      memoryCache.put("name", "memory", Option(10 days)).result()
      assert(memoryCache.get("name").result() == Some("memory"))
    }
  }

  "multi level cache" should {
    "be able to retrive" in {
      multilevelCache.put("name", "multi", Option(10 days)).result()
      assert(multilevelCache.get("name").result() == Some("multi"))
    }

    "be able to delete" in {
      multilevelCache.put("key1", "value1", Option(10 days)).result()
      assert(multilevelCache.get("key1").result() == Some("value1"))

      multilevelCache.remove("key1").result()
      assert(multilevelCache.get("key1").result() == None)
    }
  }

  "Cache" should {
    "properly cache with TTL" in {

      val cache = memoryCache

      assert(cache.get("NonExistant").result().isEmpty)

      cache.put[String]("Existant", "asd", Option(5 second)).result()
      assert(cache.get[String]("Existant").result().get === "asd")

      Thread.sleep((6 seconds).toMillis)
      assert(cache.get("Existant").result().isEmpty)
    }
    "properly remove" in {
      val cache = memoryCache

      cache.put[String]("Existant", "asd", Option(10 days)).result()
      assert(cache.get[String]("Existant").result().get === "asd")
      cache.remove("Existant").result()
      assert(cache.get("Existant").result().isEmpty)
    }
  }
}
