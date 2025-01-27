package ncreep.zio2.stacktraces

import zio.*

// scala-cli run . --main-class ncreep.zio2.stacktraces.StackApp
object StackApp extends ZIOAppDefault:

  case class Err(value: String) extends Throwable

  val partyItems = List(1, 2, 3)

  val matchingItems = List(4, 5, 6)

  def checkCondition(x: Int, y: Int)(z : Int) =
    ZIO
      .unless(x * y == z)(ZIO.succeed(true))
      .some
      .mapError(_ => Err("bad choice"))

  def attemptTake(value: Int)(other: Int) =
    ZIO.foreach(matchingItems)(checkCondition(value, other))

  def choose(value: Int) =
    ZIO.foreach(partyItems)(attemptTake(value))

  def run = choose(2)
