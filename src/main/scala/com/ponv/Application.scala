package com.ponv

import cats.effect.std.Random
import cats.effect.{IO, IOApp}
import com.ponv.bot.{ClubiumPollBot, Response}
import com.ponv.cache.VoteBanCache
import com.ponv.config.EnvConfig
import com.ponv.db.repos.{DayGameRepo, DayGameStatsRepo, KeyValueRepo, UserRepo}
import com.ponv.db.{Migrator, SqliteTransactor}
import com.ponv.handler.BaseMsgHandler
import com.ponv.handler.logging.LoggingHandler
import com.ponv.handler.set.{GetHandler, SetHandler}
import fs2.concurrent.Topic
import org.http4s.blaze.client.BlazeClientBuilder
import org.typelevel.log4cats.LoggerName
import org.typelevel.log4cats.slf4j.Slf4jLogger
import telegramium.bots.Message
import telegramium.bots.high.{Api, BotApi}

object Application extends IOApp.Simple:

  private val logger = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = for
    config       <- EnvConfig()
    random       <- Random.javaSecuritySecureRandom[IO]
    tx           <- Database(config.sqlLitePath)
    repos         = Repos(tx)
    voteBanCache <- VoteBanCache()
    handlers     <- Handlers(repos, voteBanCache, random)
    source       <- Topic[IO, Message]
    upstream     <- Topic[IO, Response]
    _            <- Listener(source, upstream, handlers).start
    _            <- runBot(config.token, source, upstream)
  yield ()

  private def runBot(token: String, source: Topic[IO, Message], upstream: Topic[IO, Response]): IO[Unit] = BlazeClientBuilder[IO].resource.use {
    httpClient =>
      given Api[IO] = BotApi(httpClient, baseUrl = s"https://api.telegram.org/bot$token")
      val bot       = new ClubiumPollBot(source)
      Responder(upstream).start.flatMap(_ => bot.start())
  }
