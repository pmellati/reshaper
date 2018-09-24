package reshaper.examples

import cats.implicits._
import reshaper._, Observe._, Effect._

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

  object state {
    case class UpdatingMachine(name: String) extends Resource
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

  // TODO: How can we handle state better?
  // TODO: Is there a race condition in the state?

  val updateMachineStep1 = (
    for {
      machine <- each[Machine]
      _       <- byId[MachineIntent](machine.name).filter(_.machine != machine)
      _       <- noneFound[state.UpdatingMachine]
    } yield machine
  ).react { machine =>
    put(state.UpdatingMachine(machine.name))
  }

  val updateMachineStep2 = (
    for {
      machine <- each[Machine]
      _       <- byId[MachineIntent](machine.name).filter(_.machine != machine)
      _       <- get[state.UpdatingMachine].filter(_.name == machine.name)
    } yield machine
  ).react(delete)

  val clearUpdatingMachineState = (
    for {
      updatingMachineState <- get[state.UpdatingMachine]
      machineName           = updatingMachineState.name
      machineIntentOpt     <- byIdOpt[MachineIntent](machineName)
      machineOpt           <- byIdOpt[Machine](machineName)
    } yield (updatingMachineState, machineIntentOpt, machineOpt)
  ).reactPartial {
    case (updatingMachineState, None, _) =>
      delete(updatingMachineState)

    case (updatingMachineState, Some(mi), Some(m)) if mi.machine == m =>
      delete(updatingMachineState)
  }
}
