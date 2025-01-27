package ncreep.zio2.renames

import zio.*
import zio.ZIO.acquireReleaseWith

trait PartyItem

trait Warehouse
trait Worker

val openWarehouse: UIO[Warehouse] = ???
def closeWarehouse(warehouse: Warehouse): UIO[Unit] = ???
def wakeupWorker(): Worker = ???
def workerIsAlert(error: Throwable): UIO[Boolean] = ???
def submit(worker: Worker)(item: PartyItem): Task[Unit] = ???

def prepareParty(items: Chunk[PartyItem]) =
  acquireReleaseWith(openWarehouse)(closeWarehouse): warehouse =>
    for
      worker <- ZIO.attempt(wakeupWorker()).retryUntilZIO(workerIsAlert)
      _ <- ZIO.foreachParDiscard(items)(submit(worker)).withParallelism(16)

    yield ()
