package reshaper

import cats.implicits._
import cats.Monad

// Do we have a definition for a resource?
trait Resource


sealed trait Observe[+A]

object Observe {
  implicit val observeMonad: Monad[Observe] = ???

  implicit class ObserveSyntax[A](self: Observe[A]) {
    def react(f: A => Effect): Reaction =
      Reaction[A](self)(f)

    def reactPartial(pf: PartialFunction[A, Effect]): Reaction =
      Reaction[A](self.filter(pf.isDefinedAt))(pf)

    def filter(pred: A => Boolean): Observe[A] =
      self.flatMap { a =>
        if(pred(a)) pure(a)
        else        cancel
      }

    def void: Observe[Unit] =
      self >> unit
  }

  def pure[A](a: A): Observe[A] = ???
  val cancel: Observe[Nothing] = ???

  def each[A]: Observe[A] = ???
  def all[A]: Observe[Set[A]] = ???

  // TODO: What the hell is this?
  def get[A]: Observe[A] =
    all[A].filter(_.size == 1).map(_.head)

  def getOpt[A]: Observe[Option[A]] =
    all[A].filter(_.size <= 1).map(_.headOption)

  def noneFound[A]: Observe[Unit] =
    all[A].filter(_.isEmpty).void

  def byIdOpt[A](id: String): Observe[Option[A]] = ???

  val unit: Observe[Unit] = pure[Unit](())

  def byId[A](id: String): Observe[A] =
    byIdOpt[A](id).flatMap {
      case Some(a) => pure(a)
      case None    => cancel
    }

  def byIdNotFound[A](id: String): Observe[Unit] =
    byIdOpt[A](id).filter(_.isEmpty).void

  def continueIf(cond: Boolean): Observe[Unit] =
    unit.filter(_ => cond)
}


sealed trait Effect

object Effect {
  def put(r: Resource): Effect = ???
  def delete(r: Resource): Effect = ???
  def noOp: Effect = ???
}


sealed trait Reaction

object Reaction {
  def apply[O](o: Observe[O])(f: O => Effect): Reaction = ???
}
