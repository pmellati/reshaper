package reshaper

import cats.implicits._
import cats.Monad

// Do we have a definition for a resource?
trait Resource


sealed trait Observe[A]

object Observe {
  implicit val observeMonad: Monad[Observe] = ???

  implicit class ObserveSyntax[A](observe: Observe[A]) {
    def react(f: A => Effect): Reaction[A] =
      Reaction[A](observe)(f)
  }

  def each[A]: Observe[A] = ???
  def find[A](pred: A => Boolean): Observe[Option[A]] = ???

  def ifNotExists[A](pred: A => Boolean): Observe[Unit] =
    find(pred) >>= { optA =>
      if(optA.isDefined) Monad[Observe].unit
      else               ???
    }
}


sealed trait Effect

object Effect {
  def put(r: Resource): Effect = ???
  def delete(r: Resource): Effect = ???
  def noOp: Effect = ???
}


sealed trait Reaction[A]

object Reaction {
  def apply[O](o: Observe[O])(f: O => Effect): Reaction[O] = ???
}
