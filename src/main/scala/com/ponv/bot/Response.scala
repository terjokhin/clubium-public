package com.ponv.bot

import cats.effect.IO
import cats.syntax.traverse._
import cats.instances.list._
import telegramium.bots.client.Methods
import telegramium.bots.high.implicits.*
import telegramium.bots.high.{Api, LongPollBot}
import telegramium.bots.{ChatId, ChatPermissions, InputLinkFile}

enum Response:
  case Empty                                                                extends Response
  case Text(chatId: ChatId, text: String, replyTo: Option[Int])             extends Response
  case Sticker(chatId: ChatId, fileId: String, replyTo: Option[Int])        extends Response
  case Animation(chatId: ChatId, animationId: String, replyTo: Option[Int]) extends Response
  case Restrict(chatId: ChatId, userId: Long, until: Int)                   extends Response
  case Two(first: Response, second: Response)                               extends Response
  case Many(responses: List[Response])                                      extends Response
  case Audio(chatId: ChatId, fileId: String)                                extends Response

object Response extends Methods:

  val Silence = ChatPermissions(canSendMessages = Some(false))

  def sendResponse(r: Response)(using Api[IO]): IO[Unit] = r match
    case Response.Empty                              => IO.unit
    case Response.Text(chatId, value, replyTo)       => sendMessage(chatId, value, replyToMessageId = replyTo).exec.void
    case Response.Sticker(chatId, fileId, replyTo)   => sendSticker(chatId, InputLinkFile(fileId), replyToMessageId = replyTo).exec.void
    case Response.Animation(chatId, fileId, replyTo) => sendAnimation(chatId, InputLinkFile(fileId), replyToMessageId = replyTo).exec.void
    case Response.Restrict(chatId, userId, until)    => restrictChatMember(chatId, userId, Silence, Some(until)).exec.void
    case Response.Audio(chatId, fileId)              => sendAudio(chatId, InputLinkFile(fileId)).exec.void
    case Response.Many(responses)                    => responses.traverse(sendResponse).void
    case Response.Two(first, second)                 => sendResponse(Response.Many(first :: second :: Nil))
