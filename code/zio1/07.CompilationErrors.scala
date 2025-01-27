package ncreep.zio1.compilation_errors

import zio._
import zio.blocking.*
import zio.logging.*

trait AppLogger extends Logger[String]

val stuff: ZIO[Blocking & Logging, Nothing, Int] = ???
val logger: AppLogger = ???

// val errorChannel = ZIO.fail("boom!").orDie
//
// val valueChannel = ZIO.succeed(Set(1, 2, 3)).head
//
// val envChannel = stuff.provide(logger)
