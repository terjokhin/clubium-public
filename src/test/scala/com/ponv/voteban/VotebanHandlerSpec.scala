package com.ponv.voteban

import cats.effect.IO
import cats.effect.std.Random
import cats.syntax.option.*
import com.ponv.bot.Response
import com.ponv.cache.VoteBanCache
import com.ponv.handler.voteban.VotebanHandler
import com.ponv.handler.voteban.VotebanHandler.{banMessage, hatersMessage}
import munit.CatsEffectSuite
import telegramium.bots.{Chat, ChatIntId, Message, User}

class VotebanHandlerSpec extends CatsEffectSuite:

  private val banDuration       = 60
  private val userToBan         = User(1, false, "target")
  private val snitchUser        = User(2, false, "snitch")
  private val anotherSnitchUser = User(3, false, "another snitch")
  private val messageToReply    = textToMessage("annoying text").copy(messageId = 1, from = userToBan.some)
  private val chat              = Chat(id = 1L, "chat")
  private val chatId            = ChatIntId(chat.id)
  private val random97          = Random.scalaUtilRandomSeedInt[IO](1)  // generates 97 on the first call
  private val random15          = Random.scalaUtilRandomSeedInt[IO](52) // generates 15 on the second call, use 42 to generate 15 on the first call

  test("Should return correct progress message if votes are not enough to ban") {
    val expectedText = VotebanHandler.progressMessage(userToBan, 1, 2)
    val expected     = Response.Text(ChatIntId(chat.id), expectedText, 2.some)
    val msg          = textToMessage(VotebanHandler.Cmd).copy(
      messageId = 2,
      from = snitchUser.some,
      replyToMessage = messageToReply.some
    )

    val f = for
      cache         <- VoteBanCache()
      random        <- random97
      votebanHandler = new VotebanHandler(cache, random, 2, banDuration)
      response      <- votebanHandler.proc(msg)
    yield response

    f.assertEquals(expected)
  }

  test("Should return duplicate if same user trying to vote repeatedly") {
    val expected = Response.Text(chatId, VotebanHandler.duplicate, 3.some)
    val msg      = textToMessage(VotebanHandler.Cmd).copy(
      messageId = 2,
      from = snitchUser.some,
      replyToMessage = messageToReply.some
    )

    val f = for
      cache         <- VoteBanCache()
      random        <- random97
      votebanHandler = new VotebanHandler(cache, random, 2, banDuration)
      _             <- votebanHandler.proc(msg)
      response      <- votebanHandler.proc(msg.copy(messageId = 3))
    yield response

    f.assertEquals(expected)
  }

  test("Should ban if there are enough votes for it") {
    val text     = Response.Text(chatId, banMessage(userToBan), replyTo = None)
    val restrict = Response.Restrict(chatId, userToBan.id, messageToReply.date + banDuration)
    val expected = Response.Two(text, restrict)
    val msg1     = textToMessage(VotebanHandler.Cmd).copy(
      messageId = 2,
      from = snitchUser.some,
      replyToMessage = messageToReply.some
    )
    val msg2     = textToMessage(VotebanHandler.Cmd).copy(
      messageId = 3,
      from = anotherSnitchUser.some,
      replyToMessage = messageToReply.some
    )

    val f = for
      cache         <- VoteBanCache()
      random        <- random97
      votebanHandler = new VotebanHandler(cache, random, 2, banDuration)
      _             <- votebanHandler.proc(msg1)
      response      <- votebanHandler.proc(msg2)
    yield response

    f.assertEquals(expected)
  }

  test("Should ban voters if there are enough votes for it with a 15% chance only") {
    val text      = Response.Text(chatId, hatersMessage(userToBan), replyTo = None)
    val restricts = List(
      Response.Restrict(chatId, snitchUser.id, messageToReply.date + banDuration),
      Response.Restrict(chatId, anotherSnitchUser.id, messageToReply.date + banDuration)
    )
    val expected  = Response.Many(text :: restricts)
    val msg1      = textToMessage(VotebanHandler.Cmd).copy(
      messageId = 2,
      from = snitchUser.some,
      replyToMessage = messageToReply.some
    )
    val msg2      = textToMessage(VotebanHandler.Cmd).copy(
      messageId = 3,
      from = anotherSnitchUser.some,
      replyToMessage = messageToReply.some
    )

    val f = for
      cache         <- VoteBanCache()
      random        <- random15
      votebanHandler = new VotebanHandler(cache, random, 2, banDuration)
      _             <- votebanHandler.proc(msg1)
      response      <- votebanHandler.proc(msg2)
    yield response

    f.assertEquals(expected)
  }

  private def textToMessage(text: String): Message =
    Message(
      messageId = -1,
      date = 1,
      chat = chat,
      text = text.some
    )
