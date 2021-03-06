// build.sc
import mill._, scalalib._

object reshaper extends ScalaModule { main =>
  def scalaVersion = "2.12.6"

  def ivyDeps = Agg(
    ivy"co.fs2::fs2-core:1.0.0"
  )

  def scalacOptions = Seq(
    "-Ypartial-unification",   // Needed by cats-core.
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked",
    "-language:_",
    "-Ywarn-unused:_",
    "-Xlint:nullary-override,nullary-unit,unsound-match,package-object-classes"
  )

  def scalacPluginIvyDeps = Agg(
    ivy"org.spire-math::kind-projector:0.9.8"
  )

  object test extends Tests {
    def ivyDeps =
      main.ivyDeps() ++
      Agg(
        ivy"org.specs2::specs2-core:4.3.4"
      )

    def testFrameworks = Seq("org.specs2.runner.Specs2Framework")
  }
}
