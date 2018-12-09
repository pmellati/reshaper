package reshaper

import cats.effect.IO

import reshaper.observe.Observe

// Do we have a definition for a resource?
trait Resource

sealed trait Effect

object Effect {
  def put[A : Affect](a: A): Effect =
    Put(a, implicitly[Affect[A]])

  def delete[A : Affect](id: String): Effect =
    Delete(id, implicitly[Affect[A]])

  case class Put[A](a: A, affect: Affect[A])          extends Effect
  case class Delete[A](id: String, affect: Affect[A]) extends Effect
}

trait Affect[A] {
  def put(a: A): IO[Unit]
  def delete(id: String): IO[Unit]
}

case class Reaction[O](observation: Observe[O], effect: O => Effect)

object Interpreter {
  def run(reactions: List[Reaction[_]]): Unit = {
    ???
  }
}