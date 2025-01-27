package ncreep.zio2.testing

import zio.*
import zio.test.*

class TestDB:
  def createTable(schema: String): UIO[Table] =
    Live.live(Random.nextLongBounded(10000))
      .map(id => Table(id = id, schema = schema))
      .debug("Created table")

  def dropTable(table: Table): UIO[Unit] =
    ZIO.debug(s"Dropping table ${table.id}")

object TestDB:
  val layer: ZLayer[Any, Nothing, TestDB] =
    val before =
      ZIO.debug("Starting database").as(TestDB())
    val after = ZIO.debug("Stopping database")

    ZLayer.scoped(ZIO.acquireRelease(before)(_ => after))

case class Table(id: Long, schema: String)

object Table:

  def layer(schema: String): ZLayer[TestDB, Nothing, Table] =
    val before =
      for
        db <- ZIO.service[TestDB]
        table <- db.createTable(schema)
      yield table

    def after(table: Table) =
      for
        db <- ZIO.service[TestDB]
        _ <- db.dropTable(table)
      yield ()

    ZLayer.scoped(ZIO.acquireRelease(before)(after))

def withTable[R: Tag, E, A](schema: String)(f: Table => ZIO[R, E, A]): ZIO[TestDB & R, E, A] =
  Table.layer(schema)(ZIO.service[Table].flatMap(f))

abstract class SharedDBSpec extends ZIOSpec[TestDB]:
  val bootstrap: ZLayer[Any, Nothing, TestDB] = TestDB.layer

// scala-cli test .
object Suite1 extends SharedDBSpec:
  val spec =
    suiteAll("suite1"):
      test("test1"):
        withTable("schema1-1"): table =>
          assertTrue(table.schema == "schema1-1")

      test("test2"):
        withTable("schema1-2"): table =>
          assertTrue(table.schema == "schema1-2")

object Suite2 extends SharedDBSpec:
  val spec =
    suiteAll("suite2"):
      test("test1"):
        withTable("schema2-1"): table =>
          assertTrue(table.schema == "schema2-1")

      test("test2"):
        withTable("schema2-2"): table =>
          assertTrue(table.schema == "schema2-2")
