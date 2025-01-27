package ncreep.zio2.builtin

import zio.UIO
import zio.Schedule
import zio.*

class WarehouseManager:
  val pingWorker: UIO[Unit] = ???

  def keepAwake: UIO[Unit] =
    pingWorker
      .repeat(Schedule.fibonacci(1.second).unit)
