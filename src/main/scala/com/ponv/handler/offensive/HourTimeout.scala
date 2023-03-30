package com.ponv.handler.offensive

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.handler.BaseMsgHandler
import telegramium.bots.Message

class HourTimeout extends BaseMsgHandler:

  override def proc(msg: Message): IO[Response] =
    if msg.text.contains("/timeout")
    then IO.pure(Response.Two(msg.replyText("See you in 1 hour!"), msg.restrict(3600)))
    else IO.pure(Response.Empty)
