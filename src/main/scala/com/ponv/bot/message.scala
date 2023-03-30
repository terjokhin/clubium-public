package com.ponv.bot

import com.ponv.model.PonvUser
import telegramium.bots.*

extension (msg: Message) {

  def chatId: ChatId = ChatIntId(msg.chat.id)

  def replyText(text: String): Response = Response.Text(msg.chatId, text, Some(msg.messageId))

  def replySticker(fileId: String): Response = Response.Sticker(msg.chatId, fileId, Some(msg.messageId))

  def replyAnimation(fileId: String): Response = Response.Animation(msg.chatId, fileId, Some(msg.messageId))

  def replyAudio(filedId: String): Response = Response.Audio(msg.chatId, filedId)

  def restrict(forSeconds: Int): Response = Response.Restrict(msg.chatId, msg.from.map(_.id).getOrElse(-1L), msg.date + forSeconds)
}
