package com.ponv.db.repos

import cats.effect.IO
import doobie.*
import doobie.implicits.*

trait DayGameRepo:
  def addParticipant(userId: Long): IO[Unit]
  def exists(userId: Long): IO[Boolean]
  def removeParticipant(userId: Long): IO[Unit]
  def randomPick: IO[Long]
  def countParticipants: IO[Int]

object DayGameRepo:

  def apply(xa: Transactor[IO]): DayGameRepo = new DayGameRepoImpl(xa)

  private final class DayGameRepoImpl(xa: Transactor[IO]) extends DayGameRepo:
    override def addParticipant(userId: Long): IO[Unit] =
      sql"INSERT OR REPLACE INTO game VALUES($userId)".update.run.transact(xa).void

    override def exists(userId: Long): IO[Boolean] =
      sql"SELECT * FROM game WHERE id = $userId".query[Long].option.transact(xa).map(_.nonEmpty)

    override def removeParticipant(userId: Long): IO[Unit] =
      sql"DELETE FROM game WHERE id = $userId".update.run.transact(xa).void

    override def randomPick: IO[Long] =
      sql"SELECT * FROM game ORDER BY RANDOM() LIMIT 1".query[Long].unique.transact(xa)

    override def countParticipants: IO[Int] =
      sql"SELECT COUNT(*) FROM game".query[Int].unique.transact(xa)
