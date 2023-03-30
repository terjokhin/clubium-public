package com.ponv.handler.logging

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.UserRepo
import com.ponv.handler.BaseMsgHandler
import com.ponv.model.PonvUser
import telegramium.bots.Message

class UserLogging(ur: UserRepo) extends BaseMsgHandler:

  override def proc(msg: Message): IO[Response] = ponvUser(msg) match
    case Some(pUser) => ur.upsert(pUser).as(Response.Empty)
    case None        => IO.pure(Response.Empty)
