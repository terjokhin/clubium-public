package com.ponv

import cats.effect.IO
import com.ponv.db.repos.*
import doobie.Transactor

final case class Repos(
  keyValueRepo: KeyValueRepo,
  dayGameRepo: DayGameRepo,
  dayGameStatsRepo: DayGameStatsRepo,
  userRepo: UserRepo
)

object Repos:

  def apply(tx: Transactor[IO]): Repos = Repos(
    KeyValueRepo(tx),
    DayGameRepo(tx),
    DayGameStatsRepo(tx),
    UserRepo(tx)
  )
