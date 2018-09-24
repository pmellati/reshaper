package reshaper.examples

import cats.implicits._
import reshaper._, Observe._, Effect._

object Machines {
  case class MachineIntent(id: String, imageUrl: String) extends Resource
  case class Machine(id: String, imageUrl: String)       extends Resource

  /** Simpler version. Based on the assumption that putting a machine is idempotent. */
  val createMachine = each[MachineIntent].react { mi => put(Machine(mi.id, mi.imageUrl)) }

  // TODO: Implement withFilter or generally filtering?
  val deleteMachine = (
    for {
      machine <- each[Machine]
      _       <- ifNotExists[MachineIntent](_.id == machine.id)
    } yield machine
    ).react(delete)
}
