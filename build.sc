// build.sc
import mill._, scalalib._

object reshaper extends ScalaModule {
  def scalaVersion = "2.12.6"

  object test extends Tests {
    def ivyDeps = Agg(
      ivy"org.specs2::specs2-core:4.3.4"
    )

    def testFrameworks = Seq("org.specs2.runner.Specs2Framework")
  }
}
