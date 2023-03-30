package com.ponv.handler.logging

import cats.Show
import cats.effect.IO
import cats.syntax.show.*
import com.ponv.bot.*
import com.ponv.handler.BaseMsgHandler
import org.typelevel.log4cats.slf4j.Slf4jLogger
import telegramium.bots.{Chat, Message, User}

class LoggingHandler extends BaseMsgHandler:
  private val logger = Slf4jLogger.getLoggerFromName[IO]("MSG_LOGGER")

  override def proc(msg: Message): IO[Response] =
    logger.info(LoggingHandler.show.show(msg)).as(Response.Empty)

object LoggingHandler:

  implicit val show: Show[Message] = info => {
    val username = info.from.map { u => name(u) }.getOrElse("")
    val text     = info.text.map { t => s"Text: $t" }.getOrElse("")
    val sticker  = info.sticker
      .map { s =>
        s"Sticker(fileId: ${s.fileId}, fileUniqueId: ${s.fileUniqueId}, isVideo: ${s.isVideo}, isAnimated ${s.isAnimated})"
      }
      .getOrElse("")
    val video    = info.video
      .map { v =>
        s"Video(fileId: ${v.fileId}, fileUniqueId: ${v.fileUniqueId})"
      }
      .getOrElse("")

    val animation = info.animation
      .map { a =>
        s"Animation(fileId: ${a.fileId}, fileUniqueId: ${a.fileUniqueId}"
      }
      .getOrElse("")

    val audio = info.audio
      .map { a =>
        s"Audio(fileId: ${a.fileId}, fileUniqueId: ${a.fileUniqueId}"
      }
      .getOrElse("")

    val chat = chatInfo(info.chat)

    List(chat, username, text, sticker, video, animation, audio).filterNot(_.isEmpty).mkString(" ")
  }

  private def name(user: User): String =
    val id = user.id.toString
    val fn = user.firstName
    val ln = user.lastName.getOrElse("")
    val un = user.username.map(x => s"($x)").getOrElse("")
    List(id, fn, ln, un).filterNot(_.isEmpty).mkString(" ")

  private def chatInfo(chat: Chat): String =
    val id       = s"Chat Id: ${chat.id.toString}"
    val name     = chat.title.map(x => s"Chat Title: $x").getOrElse("")
    val username = chat.username.map(x => s"Chat Username: $x").getOrElse("")
    List(id, name, username).filterNot(_.isEmpty).mkString(" ")
