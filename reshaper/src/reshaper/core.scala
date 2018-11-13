package reshaper

import reshaper.observe.Observe

// Do we have a definition for a resource?
trait Resource

sealed trait Effect

object Effect {
  def put(r: Resource): Effect = ???
  def delete(r: Resource): Effect = ???
  def noOp: Effect = ???
}

case class Reaction[O](observation: Observe[O], effect: O => Effect)
