package de.zalando.cachecool

import com.typesafe.config.{Config, ConfigObject, ConfigValue}

/**
  * Created by ssarabadani on 06/06/16.
  */
case class CacheDefinition(factoryName: String, isDefault: Boolean, factoryConfig: ConfigObject)

object CacheDefinition {
  def apply(configValue: ConfigValue): CacheDefinition = {
    val c: Config = configValue.atKey("c")
    val factoryName: String = c.getString("c.factory")
    val isDefault: Boolean = c.hasPath("c.default") match {
      case true => c.getBoolean("c.default")
      case _ => false
    }
    val factoryConfig = c.getObject("c.config")

    CacheDefinition(factoryName, isDefault, factoryConfig)
  }
}
