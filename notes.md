# Design goals

* Loosely coupled, declarative program flow
  + The system can create an efficient infinite run loop
  + Simplified logic definition due to loose coupling
* Utilize cluster state as much as possible (observe the cluster directly)
  + Manage as little state as possible
  + Be able to extract the state of the cluster at will
* First class support for human interaction and decisioning (no-ops aware)
* Emphasize idempotence
  + Requires less state management. Makes loss of state less costly.
* Resource life-cycle hooks?
  + Makes language more expressive. E.g. we can do something _before_ a resource is deleted!
  + Helps with decoupling
* Interpreted execution
  + Execution vs Simulation
  + The use doesn't do effects manually. The system is responsible for it.

# Questions

* Reactive vs dependency-based resource allocation?
  + Do they essentially differ?
  + Which is more expressive?
  + What are the limitations?
* How can live updates be supported?
* Is it possible to see a _"plan"_ given a command / intent?
* Testing possibilities?
* How many resources can be PUT in a single reaction?
