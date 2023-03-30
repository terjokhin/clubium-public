package com.ponv.db

import cats.effect.IO
import doobie.util.transactor.Transactor
import munit.CatsEffectSuite

import java.io.File

trait PerSuiteDB extends CatsEffectSuite:

  val xa = new Fixture[Transactor.Aux[IO, Unit]]("database-tests") {

    var tempFile: File                       = null
    var dbPath: String                       = null
    var transactor: Transactor.Aux[IO, Unit] = null

    def apply() = transactor

    override def beforeAll(): Unit = {
      transactor = {
        tempFile = File.createTempFile("temp", "db")
        dbPath = tempFile.getAbsolutePath
        val transactor = SqliteTransactor.transactor(dbPath)
        Migrator.migrate(transactor, dbPath).unsafeRunSync()
        transactor
      }
    }

    override def afterAll(): Unit =
      transactor = null
      tempFile.delete()
      tempFile = null
      dbPath = null
  }

  override def munitFixtures = List(xa)
