package ncreep.zio1.stacktraces

import zio.*

// scala-cli run . --main-class ncreep.zio1.stacktraces.StackApp
object StackApp extends zio.App:

  case class Err(value: String) extends Throwable

  val partyItems = List(1, 2, 3)

  val matchingItems = List(4, 5, 6)

  def checkCondition(x: Int, y: Int)(z : Int) =
    ZIO
      .succeed(Some(true).filter(_ => x * y == z))
      .get
      .mapError(_ => Err("bad choice"))

  def attemptTake(value: Int)(other: Int) =
    ZIO.foreach(matchingItems)(checkCondition(value, other))

  def choose(value: Int) =
    ZIO.foreach(partyItems)(attemptTake(value))

  def run(args: List[String]) = choose(2).exitCode
