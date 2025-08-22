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

package io.github.rafafrdz.tabletrace

import cats.Applicative
import cats.effect.{Async, Sync}
import cats.implicits._
import io.github.rafafrdz.tabletrace.api.domain.errors.APIError
import org.http4s.{EntityEncoder, Response, Status}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigReader.{Result => CResult}
import pureconfig.error.ConfigReaderFailures
package object api {

  /**
   * Alias for results that can fail with an APIError.
   */
  type Result[A] = Either[APIError, A]

  /**
   * Implicit logger instance using SLF4J.
   * This allows logging within any `F[_]` that has a `Sync` instance.
   */
  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  /**
   * Helper function that converts a PureConfig `Result` into a failed effect if the config is invalid.
   * @param result A result from PureConfig parsing
   * @return Effectful value or raised error if parsing failed
   */
  private def handlerError[F[_]: Async, T](result: CResult[T]): F[T] =
    result match {
      case Right(dbc) => Async[F].pure(dbc)
      case Left(err: ConfigReaderFailures) =>
        new NoSuchFieldError(err.prettyPrint(0)).raiseError[F, T]
    }

  /**
   * Enrichment method to easily convert PureConfig result into an effectful value with error handling.
   */
  implicit class ResultImplicits[T](result: CResult[T]) {
    def handler[F[_]: Async]: F[T] = handlerError(result)
  }

  /**
   * Enrichment for converting a Result into an HTTP response with appropriate status code.
   * Handles common HTTP statuses like 400, 404, 422, 401, and 500.
   * @param statusWhenRight HTTP status to return if the result is Right(value)
   */
  implicit class ApiResponseEnrich[T](response: Result[T]) {
    def toHttp4sResponse[F[_]: Applicative](
        statusWhenRight: Status
    )(implicit encoder: EntityEncoder[F, T]): F[Response[F]] = {
      implicitly[Applicative[F]].pure(
        response.fold(
          {
            case APIError(Status.BadRequest, message) =>
              Response[F](status = Status.BadRequest).withEntity(message)
            case APIError(Status.NotFound, message) =>
              Response[F](status = Status.NotFound).withEntity(message)
            case APIError(Status.UnprocessableEntity, message) =>
              Response[F](status = Status.UnprocessableEntity).withEntity(message)
            case APIError(Status.Unauthorized, message) =>
              Response[F](status = Status.Unauthorized).withEntity(message)
            case apiError =>
              Response[F](status = Status.InternalServerError).withEntity(apiError.message)
          },
          {
            case ()   => Response[F](status = Status.NoContent)
            case body => Response[F](status = statusWhenRight).withEntity(body)
          }
        )
      )
    }
  }

  /**
   * Global error handler for converting uncaught exceptions into 500 Internal Server Error responses.
   * Logs the full exception stack trace and builds a response based on it.
   */
  def errorHandler[F[_]: Async: Logger]: PartialFunction[Throwable, F[Response[F]]] = {
    case th: Throwable =>
      Logger[F].error(th)(th.getMessage) *> APIError(
        Status.InternalServerError,
        s"InternalServerError: $th"
      )
        .asLeft[Unit]
        .toHttp4sResponse(Status.InternalServerError)
  }
}
