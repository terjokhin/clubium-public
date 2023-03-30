package com.ponv.handler.game

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.DayGameStatsRepo
import com.ponv.handler.BaseMsgHandler
import com.ponv.handler.game.StatMeHandler.answer
import com.ponv.model.PonvUser
import telegramium.bots.Message

class StatMeHandler(l: DayGameStatsRepo) extends BaseMsgHandler:

  override def proc(msg: Message): IO[Response] = expects(msg) match
    case Some(u) => l.numberOfWins(u.id).map(answer(msg, _, u.id))
    case None    => IO.pure(Response.Empty)

  private def expects(msg: Message): Option[PonvUser] = for
    t <- msg.text
    if t.startsWith("/statsme")
    u <- ponvUser(msg)
  yield u

object StatMeHandler:

  def answer(msg: Message, wins: Int, usedId: Long): Response = wins match
    case 0 => msg.replyText("You don't win so far!")
    case 1 => msg.replyText("One time winner!")
    case _ => msg.replyText(s"Your score is: $wins")
