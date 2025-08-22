/*
 * Copyright 2024 Rafael Fernandez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.rafafrdz.tabletrace.api

import cats.Parallel
import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import io.github.rafafrdz.tabletrace.api.domain.errors.APIError
import org.http4s.Status
import org.typelevel.log4cats.Logger

package object handler {

  implicit def eitherTParallel[F[_]: Parallel]: Parallel[EitherT[F, APIError, *]] =
    Parallel[EitherT[F, APIError, *]]

  private[handler] implicit class ImplicitHandlerService[F[_]: Async, T](ft: F[T]) {

    def withInternalServerErrorT(message: String): EitherT[F, APIError, T] =
      ft.attemptT
        .leftSemiflatMap { e =>
          Logger[F].error(e)(message) *>
            APIError(Status.UnprocessableEntity, e.getMessage).pure[F]
        }

    def withInternalServerError(message: String): F[Either[APIError, T]] =
      withInternalServerErrorT(message).value

  }
}
