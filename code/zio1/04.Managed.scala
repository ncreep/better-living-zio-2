package ncreep.zio1.managed

import zio.ZPool
import zio.UIO
import zio.ZIO
import zio.ZManaged
import zio.Ref

trait Drink
trait DrinkOrder
trait Money
trait TableID

trait Barman:
  def prepareDrink(order: DrinkOrder): UIO[Drink]

trait Waiter:
  def takeBevarageOrder(id: TableID): UIO[List[DrinkOrder]]
  def serveDrinks(drinks: List[Drink]): UIO[Unit]
  def collectTips: UIO[Money]

object Refs:
  val waiters: Ref[Waiter] = ???
  val barmans: Ref[Barman] = ???

  def serve(id: TableID) =
    for
      waiter <- waiters.get
      orders <- waiter.takeBevarageOrder(id)
      drinks <- ZIO.foreach(orders): order =>
        for
          barman <- barmans.get
          drink <- barman.prepareDrink(order)
        yield drink
      _ <- waiter.serveDrinks(drinks)
    yield ()

object Pool:
  val waiters: ZPool[Nothing, Waiter] = ???
  val barmans: Ref[Barman] = ???

  def serve(id: TableID) =
    for
      waiter <- waiters.get
      orders <- waiter.takeBevarageOrder(id).toManaged_
      drinks <- ZIO.foreach(orders): order =>
        for
          barman <- barmans.get
          drink <- barman.prepareDrink(order)
        yield drink
      .toManaged_
      _ <- waiter.serveDrinks(drinks).toManaged_
    yield ()
  .useNow

object Pools:
  val waiters: ZPool[Nothing, Waiter] = ???
  val barmans: ZPool[Nothing, Barman] = ???

  def serve(id: TableID) =
    for
      waiter <- waiters.get
      orders <- waiter.takeBevarageOrder(id).toManaged_
      drinks <- ZManaged.foreach(orders): order =>
        for
          barman <- barmans.get
          drink <- barman.prepareDrink(order).toManaged_
        yield drink
      _ <- waiter.serveDrinks(drinks).toManaged_
    yield ()
  .useNow
