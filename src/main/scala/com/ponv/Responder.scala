package com.ponv

import cats.effect.IO
import com.ponv.bot.*
import fs2.concurrent.Topic
import telegramium.bots.high.Api

object Responder:
  def apply(topic: Topic[IO, Response])(using api: Api[IO]): IO[Unit] =
    topic
      .subscribe(1)
      .filter(_ != Response.Empty)
      // Hack. We need to make responder and listener to be daemons with self recovery logic.
      .foreach(Response.sendResponse(_).attempt.void)
      .compile
      .drain
