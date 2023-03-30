package com.ponv.db.repos

import com.ponv.db.PerSuiteDB
import com.ponv.model.PonvUser

class UserRepoSpec extends PerSuiteDB:

  test("Should upsert an user") {
    val tx    = xa()
    val repo  = UserRepo(tx)
    val user1 = PonvUser(1L, "Vasya", Some("Pupkin"), true, 0, None)
    val user2 = user1.copy(name = "Dima", lastname = Some("Value"), username = Some("username2"), active = false, lastseen = 1)

    val p = for {
      _  <- repo.upsert(user1)
      r1 <- repo.find(user1.id)
      _  <- repo.upsert(user2)
      r2 <- repo.find(user1.id)
    } yield (r1, r2)

    p.assertEquals((Some(user1), Some(user2)))
  }

  test("Should deactivate user") {
    val tx    = xa()
    val repo  = UserRepo(tx)
    val user1 = PonvUser(101L, "Vasya", Some("Pupkin"), true, 0, None)

    val p = for {
      _ <- repo.upsert(user1)
      _ <- repo.deactivate(user1.id)
      r <- repo.get(user1.id)
    } yield r.active

    p.assertEquals(false)
  }
