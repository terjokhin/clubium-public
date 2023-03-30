package com.ponv.handler.set

import cats.effect.IO
import com.ponv.bot.*
import com.ponv.db.repos.KeyValueRepo
import com.ponv.handler.BaseMsgHandler
import com.ponv.handler.set.SetHandler.{SaveKeyValueR, SetKV}
import telegramium.bots.Message

import scala.util.matching.Regex

class SetHandler(kv: KeyValueRepo) extends BaseMsgHandler:

  def expects(msg: Message): Option[SetKV] = for
    text   <- msg.text
    result <- text match
                case SaveKeyValueR(key, value) => Some(SetKV(key, value))
                case _                         => None
  yield result

  override def proc(msg: Message): IO[Response] = expects(msg) match
    case Some(cmd) => kv.put(cmd.key, cmd.value).as(msg.replyText("Схоронил!"))
    case None      => IO.pure(Response.Empty)

object SetHandler:

  val SaveKeyValueR: Regex = "(?s)/set1\\s+(\\S*)\\s+(.*)".r

  final case class SetKV(key: String, value: String)
