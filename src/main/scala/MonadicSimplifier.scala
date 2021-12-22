/* Copyright (C) 2021 Sina Ghaffari
 * This file is part of MonadicSimplifier <https://github.com/sinaghaffari/MonadicSimplifier>.
 *
 * MonadicSimplifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonadicSimplifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonadicSimplifier.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sinaghaffari

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object MonadicSimplifier {

  class FutureEitherOps[A](futureEither: Future[Either[Throwable, A]]) {
    def ?| : Step[A] = Step(futureEither)
  }
  class FutureOps[A](future: Future[A]) {
    def ?|(implicit executionContext: ExecutionContext): Step[A] = {
      val promise = Promise[Either[Throwable, A]]()
      future.onComplete {
        case Success(v) => promise.success(Right(v))
        case Failure(e) => promise.success(Left(e))
      }
      Step(promise.future)
    }
  }
  class OptionOps[A](option: Option[A]) {
    def ?|(ex: Throwable): Step[A] = Step(Future.successful(option.toRight(ex)))
  }
  class FutureOptionOps[A](futureOption: Future[Option[A]]) {
    def ?|(ex: Throwable)(implicit executionContext: ExecutionContext): Step[A] = Step((for {
      f <- new FutureOps(futureOption).?|
      o <- f.?|(ex)
    } yield o).run)
  }
  class EitherOps[A](either: Either[Throwable, A]) {
    def ?| : Step[A] = Step(Future.successful(either))
  }
  class TryOps[A](`try`: Try[A]) {
    def ?| : Step[A] = Step(Future.successful(`try`.toEither))
  }
  class BooleanOps(boolean: Boolean) {
    def ?|(ex: Throwable): Step[Unit] = Step(Future.successful(if (boolean) Right(()) else Left(ex)))
  }
  class FutureBooleanOps(futureBoolean: Future[Boolean]) {
    def ?|(ex: Throwable)(implicit executionContext: ExecutionContext): Step[Unit] =
      Step(new FutureOps(futureBoolean).?|.flatMap(_.?|(ex)).run)
  }


  final case class Step[+A](run: Future[Either[Throwable, A]]) {
    def map[B](f: A => B)(implicit ec: ExecutionContext): Step[B] =
      copy(run = run.map(_.map(f)))

    def flatMap[B](f: A => Step[B])(implicit ec: ExecutionContext): Step[B] =
      copy(run = run.flatMap(_.fold(
        err => Future.successful(Left[Throwable, B](err)),
        succ => f(succ).run
      )))

    def withFilter(p: A => Boolean)(implicit ec: ExecutionContext): Step[A] =
      copy(run = run.filter {
        case Right(a) if p(a) => true
        case Left(e) => true
        case _ => false
      })
  }

  implicit def simplifyFutureEither[A](futureEither: Future[Either[Throwable, A]]): FutureEitherOps[A] =
    new FutureEitherOps(futureEither)
  implicit def simplifyFuture[A](future: Future[A]): FutureOps[A] = new FutureOps(future)
  implicit def simplifyOption[A](option: Option[A]): OptionOps[A] = new OptionOps(option)
  implicit def simplifyFutureOption[A](futureOption: Future[Option[A]]): FutureOptionOps[A] =
    new FutureOptionOps(futureOption)
  implicit def simplifyEither[A](either: Either[Throwable, A]): EitherOps[A] = new EitherOps(either)
  implicit def simplifyTry[A](`try`: Try[A]): TryOps[A] = new TryOps(`try`)
  implicit def simplifyBoolean(boolean: Boolean): BooleanOps = new BooleanOps(boolean)
  implicit def simplifyFutureBoolean(futureBoolean: Future[Boolean]): FutureBooleanOps =
    new FutureBooleanOps(futureBoolean)
}