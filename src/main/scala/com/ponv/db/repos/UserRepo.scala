package com.ponv.db.repos

import cats.effect.IO
import com.ponv.db.repos.UserRepo.UserNotFound
import com.ponv.model.PonvUser
import doobie.implicits.*
import doobie.util.transactor.Transactor

trait UserRepo:
  def find(id: Long): IO[Option[PonvUser]]
  def upsert(user: PonvUser): IO[Unit]
  def deactivate(id: Long): IO[Unit]

  def get(id: Long): IO[PonvUser] = find(id).flatMap {
    case Some(u) => IO.pure(u)
    case None    => IO.raiseError(UserNotFound(id))
  }

object UserRepo:

  def apply(xa: Transactor[IO]): UserRepo = new UserRepoImpl(xa)

  private final class UserRepoImpl(xa: Transactor[IO]) extends UserRepo:
    override def upsert(user: PonvUser): IO[Unit] =
      sql"INSERT OR REPLACE INTO users VALUES (${user.id}, ${user.name}, ${user.lastname}, ${user.active}, ${user.lastseen}, ${user.username})".update.run
        .transact(xa)
        .void

    override def find(id: Long): IO[Option[PonvUser]] =
      sql"SELECT * FROM users WHERE id = $id"
        .query[PonvUser]
        .option
        .transact(xa)

    override def deactivate(id: Long): IO[Unit] =
      sql"UPDATE users SET active = 0 WHERE id = $id".update.run
        .transact(xa)
        .void

  final case class UserNotFound(id: Long) extends Exception(s"Cannot find user with id $id", null, true, false)
