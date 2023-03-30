package com.ponv.handler.set

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.KeyValueRepo
import com.ponv.handler.BaseMsgHandler
import com.ponv.handler.set.GetHandler.{FindValueR, GetV}
import telegramium.bots.{ChatId, ChatIntId, Message}

import scala.util.matching.Regex

class GetHandler(kv: KeyValueRepo) extends BaseMsgHandler:

  def expects(msg: Message): Option[GetV] = for
    text   <- msg.text
    result <- text match {
                case FindValueR(key) => Some(GetV(key))
                case _               => None
              }
  yield result

  override def proc(msg: Message): IO[Response] = expects(msg) match
    case Some(cmd) =>
      kv.find(cmd.key).map {
        case Some(found) => msg.replyText(found)
        case None        => msg.replyText("404")
      }
    case None      => IO.pure(Response.Empty)

object GetHandler:

  val FindValueR: Regex = "/get1\\s+(\\S*)".r

  final case class GetV(key: String)
