package reshaper.examples.machines

import reshaper.Effect._
import reshaper._
import reshaper.observe.Observe._
import reshaper.observe.Observer

/** What we want to say:
  *
  * Create a machine as soon as the intent appears.
  *
  * Delete a machine as soon as the intent disappears.
  *
  * Update machines one at a time!
  */
object Reactions {
  case class MachineIntent(machine: Machine)         extends Resource
  case class Machine(name: String, imageUrl: String) extends Resource

  // TODO: How can we handle state better?
  object state {
    case class UpdatingMachine(machineName: String) extends Resource
  }

  def createMachine(implicit miObs: Observer[MachineIntent], mObs: Observer[Machine], mAff: Affect[Machine]) = (
    for {
      machineIntent <- each[MachineIntent]
      _             <- byIdNotFound[Machine](machineIntent.machine.name)
    } yield machineIntent.machine
  ).react(put[Machine])

  def deleteMachine(implicit miObs: Observer[MachineIntent], mObs: Observer[Machine], mAff: Affect[Machine]) = (
    for {
      machine <- each[Machine]
      _       <- byIdNotFound[MachineIntent](machine.name)
    } yield machine.name
  ).react(delete[Machine])

//  val updateMachine = (
//    for {
//      machine <- each[Machine]
//      _       <- byId[MachineIntent](machine.name).filter(_.machine != machine)
//    } yield machine
//  )
//    .react(delete)
//    .withLock(machine => "updatingMachine" -> state.UpdatingMachine(machine.name))
//
//  val releaseUpdatingMachineLock = (
//    for {
//      lock             <- byId[state.UpdatingMachine]("updatingMachine")
//      machineIntentOpt <- byIdOpt[MachineIntent](lock.machineName)
//      machineOpt       <- byIdOpt[Machine](lock.machineName)
//    } yield (lock, machineIntentOpt, machineOpt)
//  ).reactPartial {
//    case (lock, None, _) =>
//      delete(lock)
//
//    case (lock, Some(MachineIntent(desired)), Some(current)) if desired == current =>
//      delete(lock)
//  }
//
//  // TODO: Really ugly. Fix later.
//  implicit class ReactionWithLock[A](reaction: Reaction[A]) {
//    def withLock[B <: Resource](getLock: A => (String, B))(implicit obsB: Observer[B]) = (
//      for {
//        o           <- reaction.observation
//        lockWithId   = getLock(o)
//        (lockId, _)  = lockWithId
//        fetchedLock <- byIdOpt[B](lockId)
//      } yield (o, fetchedLock)
//    ).reactPartial {
//      case (o, Some(lock)) if lock == getLock(o)._2 => reaction.effect(o)
//      case (o, None)                                => put(getLock(o)._2)
//    }
//  }
}
