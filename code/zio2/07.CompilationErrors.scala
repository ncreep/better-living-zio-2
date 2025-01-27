package ncreep.zio2.compilation_errors

import zio.*

trait Blocking
trait Logger
trait AppLogger extends Logger

val stuff: ZIO[Blocking & Logger, Nothing, Int] = ???
val logger: ZLayer[Any, Nothing, AppLogger] = ???

// val errorChannel = ZIO.fail("boom!").orDie
//
// val valueChannel = ZIO.succeed(Set(1, 2, 3)).head
//
// val envChannel = stuff.provide(logger)
