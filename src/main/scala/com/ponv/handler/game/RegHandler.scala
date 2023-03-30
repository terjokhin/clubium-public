package com.ponv.handler.game

import cats.Monad
import cats.effect.IO
import cats.implicits.*
import com.ponv.bot.*
import com.ponv.db.repos.DayGameRepo
import com.ponv.handler.BaseMsgHandler
import com.ponv.handler.game.RegHandler.alreadyInTheGameResponse
import com.ponv.model.PonvUser
import telegramium.bots.Message

class RegHandler(dayGameRepo: DayGameRepo) extends BaseMsgHandler:
  override def proc(msg: Message): IO[Response] = expects(msg) match
    case Some(user) => Monad[IO].ifM(dayGameRepo.exists(user.id))(alreadyInTheGameResponse(msg), addToGame(msg, user))
    case None       => IO.pure(Response.Empty)

  private def addToGame(msg: Message, user: PonvUser): IO[Response] =
    dayGameRepo.addParticipant(user.id).as(msg.replyText("You're in!"))

  private def expects(msg: Message): Option[PonvUser] = for
    text <- msg.text
    if text.toLowerCase.startsWith("/reg")
    user <- ponvUser(msg)
  yield user

object RegHandler:
  def alreadyInTheGameResponse(msg: Message): IO[Response] = IO.pure(msg.replyText("Hey! You already participant!"))
