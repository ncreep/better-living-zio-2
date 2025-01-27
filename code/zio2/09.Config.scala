package ncreep.zio2.config

import zio.*
import zio.Config.*
import zio.config.*
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.*

case class WarehouseConfig(host: String, port: Int)

object WarehouseConfig:
  given Config[WarehouseConfig] =
    (Config.string("host") zip Config.int("port")).to[WarehouseConfig]

case class PartyPlannerConfig(apiKey: String, apiEndpoint: String)

object PartyPlannerConfig:
  given Config[PartyPlannerConfig] = deriveConfig

class WarehouseManager(config: WarehouseConfig)

object WarehouseManager:
  val layer: ZLayer[Any, Config.Error, WarehouseManager] = ZLayer.derive[WarehouseManager]

class PartyManagement(config: PartyPlannerConfig, warehouse: WarehouseManager):
  def run: UIO[Unit] = ZIO.debug("let's get this party started!")

object PartyManagement:
  val layer: ZLayer[WarehouseManager, Config.Error, PartyManagement] =
    ZLayer.derive[PartyManagement]

// scala-cli run . --main-class ncreep.zio2.config.PartyApp
object PartyApp extends ZIOAppDefault:
  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.setConfigProvider(ConfigProvider.fromResourcePath())

  val app =
    for
      greeting <- ZIO.config(Config.string("greeting"))
      _ <- Console.printLine(greeting)
      party <- ZIO.service[PartyManagement]
      _ <- party.run
    yield ()

  val run = app.provide(
    PartyManagement.layer,
    WarehouseManager.layer)
