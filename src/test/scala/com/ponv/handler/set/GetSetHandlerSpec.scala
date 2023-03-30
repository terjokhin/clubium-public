package com.ponv.handler.set

import cats.effect.IO
import com.ponv.db.repos.KeyValueRepo
import com.ponv.handler.set.GetHandler.GetV
import com.ponv.handler.set.SetHandler
import com.ponv.handler.set.SetHandler.SetKV
import telegramium.bots.{Chat, Message}

class GetSetHandlerSpec extends munit.FunSuite:

  final case class SetTestCase(name: String, input: String, expected: SetKV)
  final case class GetTestCase(name: String, input: String, expected: GetV)

  val emptyRepo = new KeyValueRepo {
    override def put(key: String, value: String): IO[Unit] = IO.unit
    override def find(key: String): IO[Option[String]]     = IO.none[String]
    override def delete(key: String): IO[Unit]             = IO.unit
  }

  val setHandler = new SetHandler(emptyRepo)
  val getHandler = new GetHandler(emptyRepo)

  val testCasesSet: List[SetTestCase] = List(
    SetTestCase("Parse simple", "/set1 key value", SetKV("key", "value")),
    SetTestCase("Parse respect unicode", "/set1 ключ значение", SetKV("ключ", "значение")),
    SetTestCase("Parse with a lot of spaces", "/set1      key     value", SetKV("key", "value")),
    SetTestCase("Parse with really long value", "/set1 key value and the rest", SetKV("key", "value and the rest")),
    SetTestCase(
      "Parse with really long value and multi lines",
      "/set1 key value \nand the \nrest",
      SetKV("key", "value \nand the \nrest")
    )
  )

  val testCasesGet: List[GetTestCase] = List(
    GetTestCase("Parse simple", "/get1 key33", GetV("key33")),
    GetTestCase("Parse with spaces", "/get1         key44", GetV("key44"))
  )

  testCasesSet.foreach { case testCase => test(testCase.name) { runSetTestCase(testCase.input, testCase.expected) } }
  testCasesGet.foreach { case testCase => test(testCase.name) { runGetTestCase(testCase.input, testCase.expected) } }

  private def runSetTestCase(input: String, expected: SetKV) =
    assert(setHandler.expects(textToMessage(input)).contains(expected))

  private def runGetTestCase(input: String, expected: GetV) =
    assert(getHandler.expects(textToMessage(input)).contains(expected))

  private def textToMessage(text: String): Message =
    Message(
      messageId = -1,
      date = 1,
      chat = Chat(id = 1L, "chat"),
      text = Some(text)
    )
