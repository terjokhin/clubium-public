package com.ponv.cache

import cats.{Monad, Monoid}
import cats.effect.IO
import cats.effect.kernel.Ref.Make
import cats.syntax.functor.*
import cats.syntax.flatMap.*
import cats.syntax.option.*

import java.time.{Duration, Instant}

trait VoteBanCacheF[F[_]]:
  import com.ponv.cache.VoteBanCache.Votes

  def getById(id: Long): F[Votes]
  def set(id: Long, votes: Votes): F[Votes]

  def reset(id: Long): F[Votes] = set(id, Votes.EMPTY)

type VoteBanCache = VoteBanCacheF[IO]

object VoteBanCache:

  def apply(): IO[VoteBanCache] = applyF[IO]()

  private def applyF[F[_]: Monad: Make](): F[VoteBanCacheF[F]] = Cache.applyF[F, Long, Votes].map(new DefImpl(_))

  opaque type Votes = (Set[Long], Option[Instant])
  object Votes:
    val EMPTY                                                     = Votes(Set.empty, None)
    def apply(voters: Set[Long], banTime: Option[Instant]): Votes = (voters, banTime)

    extension (self: Votes)
      def voters: Set[Long]               = self._1
      def banTime: Option[Instant]        = self._2
      def count: Int                      = voters.size
      def add(id: Long): Votes            = Votes(voters + id, banTime)
      def isBanned: Boolean               = banTime.nonEmpty
      def isDuplicate(id: Long): Boolean  = voters.contains(id)
      def ban: Votes                      = Votes(voters, Instant.now.some)
      def toBan(maxVotes: Int): Boolean   = count >= maxVotes
      def toUnban(duration: Int): Boolean = banTime.fold(true)(Duration.between(Instant.now, _).abs().getSeconds > duration)

  private final class DefImpl[F[_]: Monad](refMap: CacheF[F, Long, Votes]) extends VoteBanCacheF[F]:
    // https://github.com/lampepfl/dotty/issues/13470
    import Votes.*

    def getById(id: Long): F[Votes] =
      refMap.get(id).map {
        case Some(v) => v
        case None    => Votes.EMPTY
      }

    def set(id: Long, votes: Votes): F[Votes] = refMap.put(id, votes).as(votes)
