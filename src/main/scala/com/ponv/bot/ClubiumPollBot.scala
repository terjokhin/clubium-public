package com.ponv.bot

import cats.effect.IO
import cats.implicits.*
import fs2.concurrent.Topic
import org.typelevel.log4cats.slf4j.Slf4jLogger
import telegramium.bots.high.implicits.*
import telegramium.bots.high.{Api, LongPollBot}
import telegramium.bots.{ChatId, ChatIntId, Message}

class ClubiumPollBot(topic: Topic[IO, Message])(using api: Api[IO]) extends LongPollBot[IO](api):
  private val logger = Slf4jLogger.getLogger[IO]

  override def onMessage(msg: Message): IO[Unit] = topic.publish1(msg).void

  override def onError(e: Throwable): IO[Unit] = logger.error("Poll bot error got!")
