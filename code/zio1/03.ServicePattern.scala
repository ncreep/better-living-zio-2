package ncreep.zio1.service

import zio.Has
import zio.UIO
import zio.ZLayer
import ncreep.zio1.service.venue.Venue
import ncreep.zio1.service.budget.Budget
import zio.URIO
import zio.ZIO

trait Party

trait BookingStatus
trait Money

object venue:
  type Venue = Has[Venue.Service]

  object Venue:
    trait Service:
      def book: UIO[BookingStatus]

object budget:
  type Budget = Has[Budget.Service]

  object Budget:
    trait Service:
      def calc: UIO[Money]

object planner:
  type Planner = Has[Planner.Service]

  object Planner:
    trait Service:
      def planParty(budget: Money): UIO[Party]

  val live =
    ZLayer.fromServices: (venue: Venue.Service, budget: Budget.Service) =>
      new Planner.Service:
        def planParty(budget: Money): UIO[Party] =
          // use venue and budget
          ???

  def planParty(budget: Money): URIO[Planner, Party] =
    ZIO.accessM(_.get.planParty(budget))
