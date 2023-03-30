package com.ponv.handler.game

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.{DayGameStatsRepo, UserRepo}
import com.ponv.handler.BaseMsgHandler
import com.ponv.model.PonvUser
import telegramium.bots.Message

class TopTenHandler(dayGameStatsRepo: DayGameStatsRepo, userRepo: UserRepo) extends BaseMsgHandler:

  override def proc(msg: Message): IO[Response] = if expect(msg) then renderTopTen.map(msg.replyText) else IO.pure(Response.Empty)

  private def expect(msg: Message): Boolean = msg.text.contains("/top10")

  private def renderTopTen: IO[String] = for
    rawData <- dayGameStatsRepo.topTenWinners
    data    <- IO.parTraverseN(1)(rawData)(convertToString)
  yield data.mkString("\n")

  private def convertToString(userId: Long, wins: Int): IO[String] = userRepo.get(userId).map { u =>
    PonvUser.show.show(u) + " " + wins.toString
  }
