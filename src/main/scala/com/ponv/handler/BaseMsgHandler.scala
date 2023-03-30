package com.ponv.handler

import cats.Monad
import cats.effect.IO
import cats.implicits.*
import com.ponv.bot.*
import telegramium.bots.Message

trait BaseMsgHandler:
  def proc(msg: Message): IO[Response]
