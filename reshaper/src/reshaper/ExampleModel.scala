package reshaper

object ExampleModel {
  sealed trait Resource

  case class MachineIntent(id: String, imageUrl: String) extends Resource
  case class Machine(id: String, imageUrl: String)       extends Resource

  def get[A]: A = ???
  def getOption[A]: Option[A] = ???

  object Reaction {
    def apply[O <: Product](o: O)(f: O => Option[Resource]): Nothing = ???
  }

  /** Human description of first reaction:
    *
    * If a machine intent exists, but no machine with the same specs exists, put the machine.
    */
  val createMachines = Reaction(get[MachineIntent], getOption[Machine]) { case (machineIntent, machine) =>
    machine.fold[Option[Machine]](ifEmpty = Some(Machine(machineIntent.id, machineIntent.imageUrl))) { existingMachine =>
      if(machineIntent.imageUrl != existingMachine.imageUrl)
        Some(Machine(machineIntent.id, machineIntent.imageUrl))
      else
        None
    }
  }
}
