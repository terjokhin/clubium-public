package com.ponv

import cats.effect.{IO, Resource}
import com.ponv.db.{Migrator, SqliteTransactor}
import doobie.Transactor

object Database:

  def apply(sqlLitePath: String): IO[Transactor[IO]] = for
    tx <- IO(SqliteTransactor.transactor(sqlLitePath))
    _  <- Migrator.migrate(tx, sqlLitePath)
  yield tx

  def make(sqlLitePath: String): Resource[IO, Transactor[IO]] = Resource.eval(apply(sqlLitePath))
