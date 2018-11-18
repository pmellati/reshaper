package reshaper.observe

import fs2.Stream

trait Observer[F[_], A] {
  def fetchAll: Stream[F, A]
  def fetchById(id: String): F[Either[Throwable, A]]
}
