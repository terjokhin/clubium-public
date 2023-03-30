package com.ponv.handler.voteban

import cats.effect.IO
import cats.effect.std.Random
import cats.syntax.applicative.*
import cats.syntax.option.*
import com.ponv.bot.*
import com.ponv.bot.*
import com.ponv.cache.VoteBanCache
import com.ponv.handler.BaseMsgHandler
import com.ponv.utils.fromBase64
import telegramium.bots.{Message, User}

class VotebanHandler(cache: VoteBanCache, random: Random[IO], maxVotes: Int = 10, banDuration: Int = 3600) extends BaseMsgHandler:
  import VoteBanCache.Votes.*
  import VotebanHandler.*

  override def proc(msg: Message): IO[Response] = expects(msg) match
    case Some(r) => r
    case None    => Response.Empty.pure[IO]

  private def expects(msg: Message): Option[IO[Response]] =
    for
      text      <- msg.text if text.toLowerCase == VotebanHandler.Cmd
      candidate <- msg.replyToMessage.flatMap(_.from)
      voter     <- msg.from if candidate.id != BOT_ID
    yield voteResponse(msg, candidate, voter)

  private def voteResponse(msg: Message, candidate: User, voter: User): IO[Response] =
    for result <- vote(candidate.id, voter.id)
    yield result match
      case Result.Ban                   =>
        Response.Two(
          Response.Text(msg.chatId, banMessage(candidate), None),
          Response.Restrict(msg.chatId, candidate.id, msg.date + banDuration)
        )
      case Result.Bans(voters)          =>
        Response.Many(
          Response.Text(msg.chatId, hatersMessage(candidate), None) ::
          voters.toList.map(Response.Restrict(msg.chatId, _, msg.date + banDuration))
        )
      case Result.NotBan(votes, needed) => msg.replyText(progressMessage(candidate, votes, needed))
      case Result.Duplicate             => msg.replyText(duplicate)
      case Result.Stale                 => Response.Empty

  private def vote(candidate: Long, voter: Long): IO[Result] =
    cache
      .getById(candidate)
      .flatMap { votes => IO(votes.isBanned && votes.toUnban(banDuration)).ifM(cache.reset(candidate), votes.pure) }
      .flatMap { votes =>
        if votes.isBanned && !votes.toUnban(banDuration) then Result.Stale.pure[IO]
        else if votes.isDuplicate(voter) then Result.Duplicate.pure[IO]
        else
          random
            .betweenInt(0, 101)
            .flatMap { v =>
              val updated = votes.add(voter)
              v match
                // at 15% ban voters
                case v if updated.toBan(maxVotes) && v == 15 => cache.reset(candidate).as(Result.Bans(updated.voters))
                // else ban candidate
                case _                                       =>
                  if updated.toBan(maxVotes) then cache.set(candidate, updated.ban).as(Result.Ban)
                  else cache.set(candidate, updated).as(Result.NotBan(updated.count, maxVotes))
            }
      }

object VotebanHandler:
  private val BOT_ID = 1028067864L
  val Cmd            = "/voteban"

  enum Result:
    case Ban                             extends Result
    case Bans(voters: Set[Long])         extends Result
    case NotBan(votes: Int, needed: Int) extends Result
    case Duplicate                       extends Result
    case Stale                           extends Result

  def banMessage(user: User): String    = s"${user.getAppeal} you've been banned."
  def hatersMessage(user: User): String = s"${user.getAppeal}, oh wow."

  def progressMessage(user: User, votes: Int, needed: Int): String =
    s"${user.getAppeal} already got $votes out of $needed!"

  val duplicate: String = "You can't vote more than once!"
