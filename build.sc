// build.sc
import mill._, scalalib._

object reshaper extends ScalaModule { main =>
  def scalaVersion = "2.12.6"

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect:1.0.0"
  )

  def scalacOptions = Seq(
    "-Ypartial-unification"    // Needed by cats-core.
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
