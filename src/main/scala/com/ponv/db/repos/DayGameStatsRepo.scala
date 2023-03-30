package com.ponv.db.repos

import cats.effect.IO
import doobie.*
import doobie.implicits.*

trait DayGameStatsRepo:

  def lastGameTime: IO[Int]

  def lastWinner: IO[Long]

  def saveWinner(userId: Long, time: Int): IO[Unit]

  def topTenWinners: IO[List[(Long, Int)]]

  def numberOfWins(userId: Long): IO[Int]

object DayGameStatsRepo:

  def apply(xa: Transactor[IO]): DayGameStatsRepo = new DayGameStatRepoImpl(xa)

  private final class DayGameStatRepoImpl(xa: Transactor[IO]) extends DayGameStatsRepo:
    override def numberOfWins(userId: Long): IO[Int] =
      sql"SELECT Count(*) FROM game_stats WHERE user_id = $userId".query[Int].unique.transact(xa)

    override def lastGameTime: IO[Int] =
      sql"SELECT id FROM game_stats ORDER BY id DESC LIMIT 1".query[Int].option.transact(xa).map(_.getOrElse(-1))

    override def lastWinner: IO[Long] =
      sql"SELECT user_id FROM game_stats ORDER BY id DESC LIMIT 1".query[Long].option.transact(xa).map(_.getOrElse(-1L))

    override def topTenWinners: IO[List[(Long, Int)]] =
      sql"SELECT user_id, COUNT(*) as cnt FROM game_stats GROUP BY user_id ORDER BY cnt DESC LIMIT 10".query[(Long, Int)].to[List].transact(xa)

    override def saveWinner(userId: Long, time: Int): IO[Unit] =
      sql"INSERT INTO game_stats VALUES($time, $userId)".update.run.transact(xa).void
