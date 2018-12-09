package reshaper.examples.machines

import reshaper.{Affect, Interpreter}
import reshaper.observe.Observer

import Reactions._

object Execute {
  implicit val machineIntentObserver: Observer[MachineIntent] = ???
  implicit val machineObserver: Observer[Machine] = ???
  implicit val affectMachine: Affect[Machine] = ???

  def run(): Unit = {
    Interpreter.run(List(
      createMachine,
      deleteMachine
//      updateMachine,
//      releaseUpdatingMachineLock
    ))
  }
}
