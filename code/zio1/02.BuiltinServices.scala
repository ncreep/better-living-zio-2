package ncreep.zio1.builtin

import zio.clock.Clock
import zio.UIO
import zio.Schedule
import zio.duration.*
import zio.ZIO

class WarehouseManager(clock: Clock):
  val pingWorker: UIO[Unit] = ???

  def keepAwake: UIO[Unit] =
    pingWorker
      .repeat(Schedule.fibonacci(1.second).unit)
      .provide(clock)
