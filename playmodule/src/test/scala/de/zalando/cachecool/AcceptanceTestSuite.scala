package de.zalando.cachecool

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.OneServerPerSuite
import play.api.Application
import play.api.inject.Injector
import play.api.test.FakeApplication



/**
  * Created by ssarabadani on 03/06/16.
  */



trait AcceptanceTestSuite extends OneServerPerSuite with BeforeAndAfterAll {
  this: org.scalatestplus.play.OneServerPerSuite with org.scalatest.Suite =>

  val publicAddress = s"localhost:$port"

  implicit override lazy val app: Application = {
    val f = new FakeApplication()
    inject(f.injector)
    f
  }


  override protected def afterAll(): Unit = {}

  def inject(injector: Injector): Unit


}