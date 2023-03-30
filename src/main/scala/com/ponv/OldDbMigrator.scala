package com.ponv

import cats.effect.{IO, IOApp}
import com.ponv.db.SqliteTransactor
import com.ponv.db.repos.UserRepo
import com.ponv.model.PonvUser
import doobie.*
import doobie.implicits.*

object OldDbMigrator:

  val oldDb = ""
  val newDb = ""

  final case class OldUser(
    id: Int,
    name: String,
    username: Option[String],
    active: Boolean,
    admin: Boolean,
    lastseen: Int
  )

  val oldTx = SqliteTransactor.transactor(oldDb)
  val newTx = SqliteTransactor.transactor(newDb)

  val repo = UserRepo(newTx)

  val allUsers = sql"SELECT * FROM users"

  val newUsers = allUsers.query[PonvUser].to[List].transact(newTx)
  val oldUsers = allUsers.query[OldUser].to[List].transact(oldTx)

  def filterAbsent(n: List[PonvUser], o: List[OldUser]): List[PonvUser] = {
    o.filterNot(ou => n.exists(_.id == ou.id)).map { o =>
      PonvUser(id = o.id, name = o.name, lastname = None, active = o.active, lastseen = o.lastseen, username = o.username)
    }
  }

  def filterUsernames(n: List[PonvUser], o: List[OldUser]): List[PonvUser] = {
    n.filter(u => u.username.isEmpty && o.exists(ou => ou.id == u.id && ou.username.nonEmpty)).map { u =>
      u.copy(username = o.find(_.id == u.id).flatMap(_.username))
    }
  }

  def run: IO[Unit] = for
    n               <- newUsers
    o               <- oldUsers
    absent           = filterAbsent(n, o)
    _               <- IO.println(s"There are ${absent.length} old records that not exist anymore.")
    _               <- IO.parTraverseN(1)(absent)(repo.upsert)
    _               <- IO.println("All absent users been inserted")
    withoutUsernames = filterUsernames(n, o)
    _               <- IO.println(s"There are ${withoutUsernames.length} new records without username that can be restored.")
    _               <- IO.parTraverseN(1)(withoutUsernames)(repo.upsert)
    _               <- IO.println("Users without usernames been restored")
  yield ()
