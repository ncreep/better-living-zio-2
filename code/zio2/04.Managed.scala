package ncreep.zio2.managed

import zio.ZPool
import zio.UIO
import zio.ZIO
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


  val x: zio.ZLayer[Nothing, Nothing, Nothing] = ???

  def serve(id: TableID) = ZIO.scoped:
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

object Pools:
  val waiters: ZPool[Nothing, Waiter] = ???
  val barmans: ZPool[Nothing, Barman] = ???

  def serve(id: TableID) = ZIO.scoped:
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
