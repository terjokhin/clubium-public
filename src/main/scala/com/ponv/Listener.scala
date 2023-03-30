package com.ponv

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.handler.BaseMsgHandler
import fs2.concurrent.Topic
import telegramium.bots.Message

object Listener:
  def apply(source: Topic[IO, Message], upstream: Topic[IO, Response], handlers: List[BaseMsgHandler]): IO[Unit] =
    source
      .subscribe(1)
      .foreach(procMsg(_, handlers, upstream))
      .compile
      .drain

  private def procMsg(msg: Message, handlers: List[BaseMsgHandler], upstream: Topic[IO, Response]): IO[Unit] = for
    responses <- IO.parSequenceN(1)(handlers.map(_.proc(msg)))
    _         <- IO.parSequenceN(1)(responses.map(upstream.publish1))
  yield ()
