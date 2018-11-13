package reshaper.examples

import cats.implicits._
import reshaper._, observe.Observe._, Effect._

/** What we want to say:
  *
  * Create a machine as soon as the intent appears.
  *
  * Delete a machine as soon as the intent disappears.
  *
  * Update machines one at a time!
  */
object Machines {
  case class MachineIntent(machine: Machine)         extends Resource
  case class Machine(name: String, imageUrl: String) extends Resource

  // TODO: How can we handle state better?
  object state {
    case class UpdatingMachine(machineName: String) extends Resource
  }

  val createMachine = (
    for {
      machineIntent <- each[MachineIntent]
      _             <- byIdNotFound[Machine](machineIntent.machine.name)
    } yield machineIntent.machine
  ).react(put)

  val deleteMachine = (
    for {
      machine <- each[Machine]
      _       <- byIdNotFound[MachineIntent](machine.name)
    } yield machine
  ).react(delete)

  val updateMachine = (
    for {
      machine <- each[Machine]
      _       <- byId[MachineIntent](machine.name).filter(_.machine != machine)
    } yield machine
  )
    .react(delete)
    .withLock(machine => state.UpdatingMachine(machine.name))

  val releaseUpdatingMachineLock = (
    for {
      lock             <- get[state.UpdatingMachine]
      machineIntentOpt <- byIdOpt[MachineIntent](lock.machineName)
      machineOpt       <- byIdOpt[Machine](lock.machineName)
    } yield (lock, machineIntentOpt, machineOpt)
  ).reactPartial {
    case (lock, None, _) =>
      delete(lock)

    case (lock, Some(MachineIntent(desired)), Some(current)) if desired == current =>
      delete(lock)
  }

  implicit class ReactionWithLock[A](reaction: Reaction[A]) {
    def withLock[B <: Resource](lock: A => B) = (
      for {
        o    <- reaction.observation
        lock <- getOpt[B]
      } yield (o, lock)
    ).reactPartial {
      case (o, Some(`lock`)) => reaction.effect(o)
      case (o, None)         => put(lock(o))
    }
  }
}
