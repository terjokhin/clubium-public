package com.ponv.cache

import cats.Functor
import cats.effect.kernel.Ref.Make
import cats.effect.{IO, Ref}
import cats.syntax.functor.*

trait CacheF[F[_], K, V]:
  def put(k: K, v: V): F[Unit]
  def get(k: K): F[Option[V]]

type Cache[K, V] = CacheF[IO, K, V]

object Cache:
  def apply[K, V]: IO[Cache[K, V]]                          = Ref.of[IO, Map[K, V]](Map.empty[K, V]).map { new DefImpl(_) }
  def applyF[F[_]: Make: Functor, K, V]: F[CacheF[F, K, V]] = Ref.of[F, Map[K, V]](Map.empty[K, V]).map { new DefImpl(_) }

  final class DefImpl[F[_]: Functor, K, V](ref: Ref[F, Map[K, V]]) extends CacheF[F, K, V]:
    def get(k: K): F[Option[V]] = ref.get.map(_.get(k))

    def put(k: K, v: V): F[Unit] = ref.update(_.updated(k, v))
