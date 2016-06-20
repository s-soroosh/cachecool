package de.zalando.cachecool

import com.google.inject.Inject
import com.google.inject.name.Named
import org.scalatestplus.play.PlaySpec
import play.api.inject.Injector


/**
  * Created by ssarabadani on 07/06/16.
  **/
class FakeCaching @Inject()(@Named("memory") override val usedCache: GenericCache) extends CachingSupport

class CachingSupportSpec extends PlaySpec with AcceptanceTestSuite {
  var c: FakeCaching = null

  override def inject(injector: Injector): Unit = {
    c = injector.instanceOf[FakeCaching]
  }

  "injected cachecool cache" should {
    "override the default one" in {
      assert(c.usedCache.name == "memory")
    }
  }
}
