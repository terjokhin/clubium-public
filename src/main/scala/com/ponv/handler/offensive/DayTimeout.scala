package com.ponv.handler.offensive

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.handler.BaseMsgHandler
import telegramium.bots.Message

class DayTimeout extends BaseMsgHandler:
  override def proc(msg: Message): IO[Response] =
    if msg.text.contains("/timeoutday")
    then IO.pure(Response.Two(msg.replyText("See you tomorrow!"), msg.restrict(3600 * 24)))
    else IO.pure(Response.Empty)
