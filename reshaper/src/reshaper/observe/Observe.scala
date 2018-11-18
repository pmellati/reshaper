package reshaper.observe

import cats.implicits._
import cats.effect.IO
import cats.Monad

import fs2.Stream

import reshaper.{Effect, Reaction}
import Observe._

// TODO: Should we just use Stream directly and get rid of Observe?
// TODO: Use abstract effect type.
class Observe[+A](val stream: Stream[IO, A])

object Observe extends ObserveInstances with ToObserveOps {
  def pure[A](a: A): Observe[A] =
    new Observe(Stream.emit(a))

  val cancel: Observe[Nothing] =
    new Observe(Stream.empty)

  def each[A](implicit observerA: Observer[IO, A]): Observe[A] =
    new Observe(observerA.fetchAll)

  def all[A](implicit observerA: Observer[IO, A]): Observe[Vector[A]] = {
    val as: IO[Vector[A]] = observerA.fetchAll.compile.toVector
    new Observe(Stream.eval(as))
  }

  def byIdOpt[A](id: String)(implicit observerA: Observer[IO, A]): Observe[Option[A]] = {
    val ioOptA: IO[Option[A]] = observerA.fetchById(id).map(_.toOption)
    new Observe(Stream.eval(ioOptA))
  }

  def noneFound[A](implicit obsA: Observer[IO, A]): Observe[Unit] =
    all[A].filter(_.isEmpty).void

  val unit: Observe[Unit] = pure[Unit](())

  def byId[A](id: String)(implicit obsA: Observer[IO, A]): Observe[A] =
    byIdOpt[A](id).flatMap {
      case Some(a) => pure(a)
      case None    => cancel
    }

  def byIdNotFound[A](id: String)(implicit obsA: Observer[IO, A]): Observe[Unit] =
    byIdOpt[A](id).filter(_.isEmpty).void

  def continueIf(cond: Boolean): Observe[Unit] =
    unit.filter(_ => cond)
}


trait ObserveInstances {
  implicit val observeMonad: Monad[Observe] = new Monad[Observe] {
    override def flatMap[A, B](fa: Observe[A])(f: A => Observe[B]): Observe[B] =
      new Observe(fa.stream.flatMap(a => f(a).stream))

    // TODO: Is this a correct implementation?
    override def tailRecM[A, B](a: A)(f: A => Observe[Either[A, B]]): Observe[B] =
      new Observe(
        Monad[Stream[IO, ?]].tailRecM[A, B](a) { a => f(a).stream }
      )

    override def pure[A](x: A): Observe[A] =
      Observe.pure[A](x)
  }
}

trait ToObserveOps {
  implicit class ObserveOps[A](self: Observe[A]) {
    def react(f: A => Effect): Reaction[A] =
      Reaction[A](self, f)

    def reactPartial(pf: PartialFunction[A, Effect]): Reaction[A] =
      Reaction[A](self.filter(pf.isDefinedAt), pf)

    def filter(pred: A => Boolean): Observe[A] =
      self.flatMap { a =>
        if (pred(a)) pure(a)
        else cancel
      }

    def void: Observe[Unit] =
      self >> unit
  }
}
