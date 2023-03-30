package com.ponv.db

import cats.effect.IO
import doobie._
import org.flywaydb.core.Flyway
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Migrator:

  val logger = Slf4jLogger.getLogger[IO]

  def migrate(xa: Transactor[IO], path: String): IO[Unit] = xa.configure { _ =>
    IO.delay {
      val flyway = Flyway
        .configure()
        .dataSource(s"jdbc:sqlite:$path", "", "")
        .locations("migrations")
        .load()
      flyway.migrate()
    }.flatMap { result =>
      logger.info(s"Applied ${result.migrationsExecuted} migrations")
    }
  }
