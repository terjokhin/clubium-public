package com.ponv.handler.logging

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.{DayGameRepo, UserRepo}
import com.ponv.handler.BaseMsgHandler
import com.ponv.model.PonvUser
import org.typelevel.log4cats.slf4j.Slf4jLogger
import telegramium.bots.{Message, User}

class UserLeftLogging(userRepo: UserRepo, dayGameRepo: DayGameRepo) extends BaseMsgHandler:

  private val logger = Slf4jLogger.getLoggerFromName[IO]("LEFT_LOGGER")

  override def proc(msg: Message): IO[Response] = msg.leftChatMember match
    case Some(u) => logLeft(u).as(Response.Text(msg.chatId, s"${u.firstName}${u.lastName.map(x => " " + x).getOrElse("")} bye!!!", replyTo = None))
    case None    => IO.pure(Response.Empty)

  private def logLeft(user: User): IO[Unit] = for
    _ <- logger.info(s"User with id ${user.id} left.")
    _ <- userRepo.deactivate(user.id)
    _ <- dayGameRepo.removeParticipant(user.id)
  yield ()
