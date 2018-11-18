package reshaper.observe

import cats.effect.IO

import fs2.Stream

trait Observer[A] {
  def fetchAll: Stream[IO, A]
  def fetchById(id: String): IO[Option[A]]
}
