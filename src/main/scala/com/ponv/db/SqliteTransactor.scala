package com.ponv.db

import cats.effect.IO
import doobie.util.transactor.Transactor

object SqliteTransactor:

  def transactor(path: String): Transactor.Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.sqlite.JDBC",
    s"jdbc:sqlite:$path",
    "",
    ""
  )
