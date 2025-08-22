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

package io.github.rafafrdz.tabletrace.middleware.logger

import cats.ApplicativeThrow
import cats.data.Kleisli
import cats.effect.Async
import cats.implicits._
import org.http4s.server.middleware.{Logger => MLogger}
import org.http4s.{HttpApp, Response, Status}
import org.typelevel.log4cats.Logger

/**
 * Provides logging middleware for HTTP routes.
 *
 * This middleware integrates structured logging for incoming requests and errors
 * using `http4s` built-in logging and `log4cats`.
 */
object LoggerMiddleware {

  /**
   * Wraps an `HttpApp` with logging capabilities.
   *
   * @param showDetails If true, logs request details.
   * @param logHeaders  Whether to log headers.
   * @param logBodies   Whether to log request/response bodies.
   * @param httpApp     The application to be wrapped.
   * @return            An `HttpApp` with logging applied.
   */
  def impl[F[_]: Async: Logger](
      showDetails: Boolean,
      logHeaders: Boolean = true,
      logBodies: Boolean = true
  )(httpApp: HttpApp[F]): HttpApp[F] =
    withErrorLogging(
      MLogger.httpApp[F](
        logHeaders,
        logBodies,
        logAction = generateLogAction[F](showDetails)
      )(httpApp)
    )

  /**
   * Generates a log action based on the `showLog` flag.
   *
   * @param showLog If true, enables structured info logging.
   * @return        An optional logging function.
   */
  private def generateLogAction[F[_]: Async: Logger](showLog: Boolean): Option[String => F[Unit]] =
    if (showLog) Some((str: String) => Logger[F].info(str)) else None

  /**
   * Adds error-level logging for unhandled exceptions during request processing.
   *
   * @param httpApp The application to be wrapped with error logging.
   * @return        A wrapped `HttpApp` that logs and responds on failure.
   */
  def withErrorLogging[F[_]: ApplicativeThrow: Logger](httpApp: HttpApp[F]): HttpApp[F] =
    Kleisli { req =>
      httpApp.run(req).handleErrorWith { e =>
        Logger[F].error(e)(s"Error while processing request: ${req.method} ${req.uri}") *>
          Response[F](Status.InternalServerError).withEntity("Internal server error").pure[F]
      }
    }

}
