package reshaper

import cats.implicits._
import cats.Monad

// Do we have a definition for a resource?
sealed trait Resource

sealed trait Observe[A]

object Observe {
  implicit val observeMonad: Monad[Observe] = ???

  implicit class ObserveSyntax[A](observe: Observe[A]) {
    def react(f: A => Effect): Reaction[A] =
      Reaction[A](observe)(f)
  }

  def each[A]: Observe[A] = ???
  def find[A](pred: A => Boolean): Observe[Option[A]] = ???
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

object ExampleModel {
  import Observe._
  import Effect._

  case class MachineIntent(id: String, imageUrl: String) extends Resource
  case class Machine(id: String, imageUrl: String)       extends Resource

  /** Simpler version. Based on the assumption that putting a machine is idempotent. */
  val createMachine = each[MachineIntent].react {mi => put(Machine(mi.id, mi.imageUrl))}

  // TODO: Implement withFilter or generally filtering?
  val deleteMachine = (
    for {
      machine <- each[Machine]
      intent  <- find[MachineIntent](_.id == machine.id)
    } yield (machine, intent)
  ).react { case (machine, intent) =>
    if(intent.isEmpty)
      delete(machine)
    else
      noOp
  }
}
