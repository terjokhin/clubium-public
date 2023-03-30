package com.ponv.bot

import com.ponv.model.PonvUser
import telegramium.bots.Message

val ponvUser: Message => Option[PonvUser] = msg =>
  for {
    user <- msg.from
  } yield PonvUser(user.id, user.firstName, user.lastName, true, msg.date, user.username)
