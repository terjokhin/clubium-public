package com.ponv.db.repos

import cats.effect.IO
import com.ponv.db.PerSuiteDB

import scala.util.Random

class DayGameStatsRepoSpec extends PerSuiteDB {

  test("Should save an retrieve the last winner, top ten, number of wins by user") {
    val service = DayGameStatsRepo(xa())
    val userId  = 101L
    val time    = 102

    val users: List[(Long, Int)] = Map(
      0L  -> List(1),
      1L  -> List(2, 3),
      2L  -> List(4, 5, 6),
      3L  -> List(7, 8, 9, 10),
      4L  -> List(11, 12, 13, 14),
      5L  -> List(15, 16, 17, 18, 19),
      6L  -> List(20, 21, 22, 23, 24, 25),
      7L  -> List(26, 27, 28, 29, 30, 31, 32),
      8L  -> List(33, 34, 35, 36, 37, 38, 39, 40),
      9L  -> List(41, 42, 43, 44, 45, 46, 47, 48, 49),
      10L -> List(50, 51, 52, 53, 54, 55, 56, 57, 58, 59),
      11L -> List(60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70)
    ).toList
      .flatMap { case (id, list) =>
        list.map { x => id -> x }
      }

    val f = for {
      r1 <- service.lastGameTime
      _  <- IO.parTraverseN(1)(users)(user => service.saveWinner(user._1, user._2))
      r2 <- service.numberOfWins(11L)
      r3 <- service.lastGameTime
      r4 <- service.topTenWinners.map(_.take(2))
    } yield (r1, r2, r3, r4)

    f.assertEquals((-1, 11, 70, List(11L -> 11, 10L -> 10)))
  }

}
