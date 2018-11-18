package reshaper.observe

import cats.implicits._
import cats.effect.IO

import fs2.Stream

import reshaper.{Effect, Reaction}

object Observe extends ToObserveOps {
  def pure[A](a: A): Observe[A] =
    Stream.emit(a)

  val cancel: Observe[Nothing] =
    Stream.empty

  def each[A](implicit observerA: Observer[A]): Observe[A] =
    observerA.fetchAll

  def all[A](implicit observerA: Observer[A]): Observe[Vector[A]] = {
    val as: IO[Vector[A]] = observerA.fetchAll.compile.toVector
    Stream.eval(as)
  }

  def byIdOpt[A](id: String)(implicit observerA: Observer[A]): Observe[Option[A]] =
    Stream.eval(observerA.fetchById(id))

  def noneFound[A](implicit obsA: Observer[A]): Observe[Unit] =
    all[A].filter(_.isEmpty).void

  val unit: Observe[Unit] = pure[Unit](())

  def byId[A](id: String)(implicit obsA: Observer[A]): Observe[A] =
    byIdOpt[A](id).flatMap {
      case Some(a) => pure(a)
      case None    => cancel
    }

  def byIdNotFound[A](id: String)(implicit obsA: Observer[A]): Observe[Unit] =
    byIdOpt[A](id).filter(_.isEmpty).void
}


trait ToObserveOps {
  implicit class ObserveOps[A](self: Observe[A]) {
    def react(f: A => Effect): Reaction[A] =
      Reaction[A](self, f)

    def reactPartial(pf: PartialFunction[A, Effect]): Reaction[A] =
      Reaction[A](self.filter(pf.isDefinedAt), pf)
  }
}
