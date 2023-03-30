package com.ponv.db.repos

import cats.effect.IO
import doobie.implicits.*
import doobie.util.transactor.Transactor

trait KeyValueRepo:
  def put(key: String, value: String): IO[Unit]
  def find(key: String): IO[Option[String]]
  def delete(key: String): IO[Unit]

object KeyValueRepo:

  def apply(xa: Transactor[IO]): KeyValueRepo = new Impl(xa)

  private final class Impl(xa: Transactor[IO]) extends KeyValueRepo:
    override def put(key: String, value: String): IO[Unit] =
      sql"INSERT OR REPLACE INTO keyvalues VALUES($key, $value)".update.run.transact(xa).void

    override def find(key: String): IO[Option[String]] = sql"SELECT value FROM keyvalues WHERE KEY = $key".query[String].option.transact(xa)

    override def delete(key: String): IO[Unit] = sql"DELETE FROM keyvalues WHERE key = $key".update.run.transact(xa).void
