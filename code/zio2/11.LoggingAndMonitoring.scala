package ncreep.zio2.logging_and_monitoring

import zio.*
import zio.logging.*
import zio.logging.backend.SLF4J
import zio.metrics.*
import zio.metrics.connectors.datadog.*
import zio.metrics.connectors.*
import zio.metrics.jvm.DefaultJvmMetrics

case class GuestId(value: Int)

enum Approval:
  case Allowed, Denied

trait CheckIn:
  def checkInvitation(id: GuestId): UIO[Approval]

class GuestHandler(checkIn: CheckIn):

  val withLogger = loggerName("GuestHandler")
  def withId(id: GuestId) =
    ZIOAspect.annotated("guestId", id.value.toString)

  val countCheckIns = Metric.counter("checkIns").fromConst(1)
  val countApprovals = Metric.counter("approvals").contramap[List[Approval]](_.size)

  def handleGuests(ids: List[GuestId]) =
    val flow =
      for
        _ <- ZIO.logInfo("starting check-in")
        approvals <- ZIO.foreach(ids): id =>
          checkIn.checkInvitation(id) @@ withId(id)
        _ <- ZIO.logInfo("finished")
      yield approvals

    flow @@ withLogger @@ countCheckIns @@ countApprovals

object PartyApp extends ZIOAppDefault:
  val datadogClient = ZLayer.make[Unit](
    ZLayer.succeed(DatadogConfig("localhost", 8125)),
    ZLayer.succeed(MetricsConfig(100.millis)),
    datadogLayer)

  override val bootstrap =
    Runtime.removeDefaultLoggers >>>
      SLF4J.slf4j >>>
      datadogClient >>>
      Runtime.enableRuntimeMetrics >>>
      DefaultJvmMetrics.live.unit

  def run = ???
