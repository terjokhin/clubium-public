package com.ponv.handler.logging

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.UserRepo
import com.ponv.handler.BaseMsgHandler
import com.ponv.model.PonvUser
import telegramium.bots.Message

class UserWelcomeLogging extends BaseMsgHandler:

  override def proc(msg: Message): IO[Response] =
    if msg.newChatMembers.nonEmpty then IO.pure(msg.replyText("Welcome to the community!")) else IO.pure(Response.Empty)
