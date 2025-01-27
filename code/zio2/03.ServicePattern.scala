package ncreep.zio2.service

import zio.UIO
import zio.ZLayer
import zio.URIO
import zio.ZIO

trait Party

trait BookingStatus
trait Money

trait Venue:
  def book: UIO[BookingStatus]

trait Budget:
  def calc: UIO[Money]

trait Planner:
  def planParty(budget: Money): UIO[Party]

object Planner:
  def planParty(budget: Money): URIO[Planner, Party] =
    ZIO.serviceWithZIO(_.planParty(budget))

class PlannerLive(venue: Venue, budget: Budget) extends Planner:
  def planParty(budget: Money): UIO[Party] = ???

object PlannerLive:
  val layer =
    ZLayer:
      for
        venue <- ZIO.service[Venue]
        budget <- ZIO.service[Budget]
      yield PlannerLive(venue, budget)


  val layer2 = ZLayer.derive[PlannerLive]
