package com.ponv.db.repos

import com.ponv.db.PerSuiteDB

class DayGameRepoSpec extends PerSuiteDB:

  test("Should add participant, pick participant, remove participant") {
    val repo = DayGameRepo(xa())
    val id   = 1L

    val p = for {
      _  <- repo.addParticipant(id)
      e1 <- repo.exists(id)
      r1 <- repo.randomPick
      _  <- repo.removeParticipant(id)
      e2 <- repo.exists(id)
      r2 <- repo.countParticipants
    } yield (r1, r2, e1, e2)

    p.assertEquals((1L, 0, true, false))
  }
