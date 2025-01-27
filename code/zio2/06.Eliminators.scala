package ncreep.zio2.eliminators

import zio.*
import Durable.*
import java.util.UUID

case class TransactionId(value: UUID)

// we're not actually using the transaction ID, but we might if we had a real durable store
class Durable(storage: Ref.Synchronized[Map[(String, Any), Any]], transactionId: TransactionId):
  def fortify[K](
      name: String,
      key: K,
      durability: Durability)[R, E, A](zio: => ZIO[R, E, A]): ZIO[R, E, A] =
    durability match
      // in a real implementation we would to actually honor the eviction policy
      // as well as add some form of retrying
      case Durability.Save | Durability.CacheWithEviction(_) =>
        storage.modifyZIO: store =>
          val fullKey = (name, key)
          val maybeStored = store.get(fullKey)

          val newValue = ZIO.debug(s"running [$name] durably") *>
            maybeStored
              // in the real world one might want to enforce key uniqueness somehow
              .map(v => ZIO.succeed(v.asInstanceOf[A]).debug("found cached value")) // gasp...
              .getOrElse(zio.debug("fetched new value"))

          newValue.map: value =>
            (value, store.updated(fullKey, value))

  val close: UIO[Unit] = ZIO.debug("finished durable run")

object Durable:
  def fortify[K](
      name: String,
      key: K,
      durability: Durability)[R, E, A](zio: => ZIO[R, E, A]): ZIO[R & Durable, E, A] =
    ZIO.serviceWithZIO[Durable](_.fortify(name, key, durability)(zio))

  val endurate: ZLayer[Any, Nothing, Durable] = ZLayer.scoped:
    for
      store <- Ref.Synchronized.make(Map.empty[(String, Any), Any])
      transactionId <- Random.nextUUID.map(TransactionId(_)).debug("running with")
      durable <- ZIO.acquireRelease(ZIO.succeed(Durable(store, transactionId)))(_.close)
    yield durable

enum Durability:
  case Save
  case CacheWithEviction(duration: Duration)

case class VenueId(value: Int)
case class ClientId(value: Int)

enum VenueException extends Throwable:
  case CommunicationError

case class Receipt(value: Double)
enum ClientAck:
  case Okay, Reject

enum VenueStatus:
  case Available, Occupied

enum BookingStatus:
  case Success(receipt: Receipt, clientAck: ClientAck)
  case Failure

trait VenueBooking:
  def checkVenue(venueId: VenueId): ZIO[Durable, VenueException, VenueStatus]

  def payDeposit(venueId: VenueId, clientId: ClientId): ZIO[Durable, VenueException, Receipt]

  def notifyBooking(
      venueId: VenueId,
      clientId: ClientId,
      receipt: Receipt): ZIO[Durable, VenueException, ClientAck]

object VenueBooking:
  object Default extends VenueBooking:
    def checkVenue(venueId: VenueId): ZIO[Durable, VenueException, VenueStatus] =
      fortify("checkVenue", venueId, Durability.CacheWithEviction(2.second)):
        Random.nextIntBounded(10).map: status =>
          if status < 8 then VenueStatus.Available
          else VenueStatus.Occupied

    def payDeposit(venueId: VenueId, clientId: ClientId): ZIO[Durable, VenueException, Receipt] =
      fortify("payDeposit", (venueId, clientId), Durability.Save):
        ZIO.ifZIO(Random.nextBoolean)(
          onTrue = Random.nextDouble.map(Receipt(_)),
          onFalse = ZIO.fail(VenueException.CommunicationError))

    def notifyBooking(
        venueId: VenueId,
        clientId: ClientId,
        receipt: Receipt): ZIO[Durable, VenueException, ClientAck] =
      fortify("notifyBooking", (venueId, clientId, receipt), Durability.Save):
        ZIO.ifZIO(Random.nextBoolean)(
          onTrue = ZIO.succeed(ClientAck.Okay),
          onFalse = ZIO.fail(VenueException.CommunicationError))

class Booker(venue: VenueBooking):
  import VenueStatus.*

  def bookVenue(
      venueId: VenueId,
      clientId: ClientId): IO[VenueException, BookingStatus] =
    val flow =
      venue.checkVenue(venueId).flatMap:
        case Available =>
          for
            receipt <- venue.payDeposit(venueId, clientId)
            ack <- venue.notifyBooking(venueId, clientId, receipt)
          yield BookingStatus.Success(receipt, ack)

        case Occupied => ZIO.succeed(BookingStatus.Failure)
      .retryN(10)

    endurate(flow)

// scala-cli run . --main-class ncreep.zio2.eliminators.BookerApp
object BookerApp extends ZIOAppDefault:
  val booker = Booker(VenueBooking.Default)

  val run =
    booker
      .bookVenue(VenueId(54), ClientId(42))
      .debug("booking status")
      .unit
      .orDie
