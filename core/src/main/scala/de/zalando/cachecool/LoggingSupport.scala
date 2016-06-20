package de.zalando.cachecool

import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by ssarabadani on 20/06/16.
  */
trait LoggingSupport {
  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
}
