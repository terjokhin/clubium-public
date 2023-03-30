package com.ponv.handler.game

import cats.Monad
import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.{DayGameRepo, DayGameStatsRepo, UserRepo}
import com.ponv.handler.BaseMsgHandler
import com.ponv.handler.game.DayGameHandler.congratsMessage
import com.ponv.model.PonvUser
import telegramium.bots.Message

class DayGameHandler(userRepo: UserRepo, dayGameRepo: DayGameRepo, dayGameStatsRepo: DayGameStatsRepo) extends BaseMsgHandler:

  override def proc(msg: Message): IO[Response] = Monad[IO].ifM(expects(msg))(game(msg), showCurrentWinner(msg))

  private def game(msg: Message): IO[Response] = for
    winnerId  <- dayGameRepo.randomPick
    maybeUser <- userRepo.find(winnerId)
    r         <- maybeUser match {
                   case Some(u) =>
                     for _ <- dayGameStatsRepo.saveWinner(u.id, msg.date)
                     yield msg.replyText(congratsMessage(u))
                   case None    => IO.pure(msg.replyText("Player not found!"))
                 }
  yield r

  private def expects(msg: Message): IO[Boolean] = for
    last         <- dayGameStatsRepo.lastGameTime
    now           = msg.date
    timeCondition = DayGameHandler.isItNewDay(now, last)
    maybeCmd      = msg.text.exists(_.startsWith("/day"))
    result        = timeCondition && maybeCmd
  yield result

  private def showCurrentWinner(msg: Message): IO[Response] = for
    winnerId  <- dayGameStatsRepo.lastWinner
    userMaybe <- userRepo.find(winnerId)
    r          = userMaybe match
                   case Some(u) if msg.text.contains("/day") => msg.replyText(s"You're late! But the winner is ${u.name}!")
                   case _                                    => Response.Empty
  yield r

object DayGameHandler:
  private val secondsPerDay                            = 86400
  private def isItNewDay(now: Int, prev: Int): Boolean = now / secondsPerDay > prev / secondsPerDay

  def congratsMessage(u: PonvUser): String =
    val mention  = u.username.map { un => s"@$un" }.getOrElse("")
    val name     = u.name
    val lastName = u.lastname.getOrElse("")

    List(mention, "Congratulations!", name, lastName).filterNot(_.isEmpty).mkString(" ")
