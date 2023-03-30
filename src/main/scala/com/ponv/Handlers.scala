package com.ponv

import cats.effect.IO
import cats.effect.std.Random
import com.ponv.cache.VoteBanCache
import com.ponv.db.repos.*
import com.ponv.handler.BaseMsgHandler
import com.ponv.handler.game.*
import com.ponv.handler.logging.*
import com.ponv.handler.offensive.*
import com.ponv.handler.set.{GetHandler, SetHandler}
import com.ponv.handler.voteban.VotebanHandler

object Handlers:
  def apply(repos: Repos, voteBanCache: VoteBanCache, random: Random[IO]): IO[List[BaseMsgHandler]] = IO.pure {
    import repos.*

    List(
      new UserWelcomeLogging,
      new LoggingHandler,
      new UserLogging(userRepo),
      new SetHandler(keyValueRepo),
      new GetHandler(keyValueRepo),
      new RegHandler(dayGameRepo),
      new DayGameHandler(userRepo, dayGameRepo, dayGameStatsRepo),
      new StatMeHandler(dayGameStatsRepo),
      new UserLeftLogging(userRepo, dayGameRepo),
      new TopTenHandler(dayGameStatsRepo, userRepo),
      new HourTimeout,
      new DayTimeout,
      new VotebanHandler(voteBanCache, random)
    )
  }
