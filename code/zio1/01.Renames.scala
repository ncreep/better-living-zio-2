package ncreep.zio1.renames

import zio._
import zio.ZIO.bracket

trait PartyItem

trait Warehouse
trait Worker

val openWarehouse: UIO[Warehouse] = ???
def closeWarehouse(warehouse: Warehouse): UIO[Unit] = ???
def wakeupWorker(): Worker = ???
def workerIsAlert(error: Throwable): UIO[Boolean] = ???
def submit(worker: Worker)(item: PartyItem): Task[Unit] = ???

def prepareParty(items: Chunk[PartyItem]) =
  bracket(openWarehouse)(closeWarehouse): warehouse =>
    for
      worker <- IO.effect(wakeupWorker()).retryUntilM(workerIsAlert)
      _ <- IO.foreachParN_(16)(items)(submit(worker))

    yield ()
