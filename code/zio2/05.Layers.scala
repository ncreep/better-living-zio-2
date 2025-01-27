package ncreep.zio2.layers

import zio.Task
import zio.ZLayer
import zio.ZIO
import zio.ZIOAppDefault

trait Logging
trait Monitoring

trait Budget
trait Venue
trait Menu
trait Warehouse
trait Storage

trait PartyPlanner
trait EventCoordinator

class PartyManagement(planner: PartyPlanner, coordinator: EventCoordinator):
  def run: Task[Unit] = ZIO.debug("let's get this party started!")

class LoggingLive() extends Logging
class MonitoringLive() extends Monitoring

class BudgetLive(logging: Logging) extends Budget
class VenueLive(logging: Logging, monitoring: Monitoring) extends Venue
class MenuLive(logging: Logging, monitoring: Monitoring) extends Menu
class WarehouseLive(monitoring: Monitoring) extends Warehouse
class StorageLive(logging: Logging) extends Storage

class PartyPlannerLive(budget: Budget, warehouse: Warehouse) extends PartyPlanner
class EventCoordinatorLive(menu: Menu, venue: Venue) extends EventCoordinator

object LoggingLive:
  val layer = ZLayer.derive[LoggingLive]
object MonitoringLive:
  val layer = ZLayer.derive[MonitoringLive]

object BudgetLive:
  val layer = ZLayer.derive[BudgetLive]
object VenueLive:
  val layer = ZLayer.derive[VenueLive]
object MenuLive:
  val layer = ZLayer.derive[MenuLive]
object WarehouseLive:
  val layer = ZLayer.derive[WarehouseLive]
object StorageLive:
  val layer = ZLayer.derive[StorageLive]

object PartyPlannerLive:
  val layer = ZLayer.derive[PartyPlannerLive]
object EventCoordinatorLive:
  val layer = ZLayer.derive[EventCoordinatorLive]

object PartyManagement:
  val layer = ZLayer.derive[PartyManagement]

// scala-cli run . --main-class ncreep.zio2.layers.PartyApp
object PartyApp extends ZIOAppDefault:
  val app: ZIO[PartyManagement, Throwable, Unit] =
    for
      party <- ZIO.service[PartyManagement]
      _ <- party.run
    yield ()

  def run = app.provide(
    LoggingLive.layer,
    MonitoringLive.layer,
    BudgetLive.layer,
    VenueLive.layer,
    MenuLive.layer,
    WarehouseLive.layer,
    // StorageLive.layer,
    PartyPlannerLive.layer,
    EventCoordinatorLive.layer,
    PartyManagement.layer,
    // ZLayer.Debug.mermaid
  )
