package reshaper

import cats.effect.IO

import fs2.Stream

package object observe {
  type Observe[+A] = Stream[IO, A]
}
