package com.ponv.config

import cats.effect.{IO, Resource}

import scala.collection.mutable

final case class EnvConfig(token: String, sqlLitePath: String):

  override def toString: String =
    val masked = token.dropRight(3).map(_ => '*') + token.takeRight(3)
    s"EnvConfig(token = $masked, sqlLitePath = $sqlLitePath)"

object EnvConfig:

  def make: Resource[IO, EnvConfig] = Resource.eval(apply())

  private def readEnv(param: String): IO[String] = IO(sys.env(param))

  def apply(): IO[EnvConfig] = for
    token       <- readEnv("TOKEN")
    sqlLitePath <- readEnv("SQLITE_DB_PATH")
  yield EnvConfig(token, sqlLitePath)
