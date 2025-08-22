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
import cats.effect.{Async, Resource}
import com.comcast.ip4s._
import fs2.io.net.Network
import io.github.rafafrdz.tabletrace.api.config.ConfigServices
import io.github.rafafrdz.tabletrace.api.routes.AnalyzeRoutes
import io.github.rafafrdz.tabletrace.middleware.cors.CorsMiddleware
import io.github.rafafrdz.tabletrace.middleware.logger.LoggerMiddleware
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.{HttpApp, HttpRoutes}
import org.typelevel.log4cats.Logger

object Server {

  def build[F[_]: Async: Parallel: Network: Logger](config: ConfigServices): Resource[F, Server] = {

    val corsMiddleware: HttpApp[F] => HttpApp[F] =
      (httpApp: HttpApp[F]) => CorsMiddleware.impl[F](httpApp)

    val loggerMiddleware: HttpApp[F] => HttpApp[F] = (httpApp: HttpApp[F]) =>
      LoggerMiddleware.impl[F](
        config.api.showDetailedLogs,
        config.api.detailedLogConfig.logHeaders,
        config.api.detailedLogConfig.logBodys
      )(httpApp)

    val middlewares: HttpApp[F] => HttpApp[F] =
      loggerMiddleware compose corsMiddleware

    val routes: HttpRoutes[F] = AnalyzeRoutes.routes[F]

    val httpApp: HttpApp[F] = routes.orNotFound

    val server: EmberServerBuilder[F] =
      EmberServerBuilder
        .default[F]
        .withHostOption(Host.fromString(config.api.host))
        .withPort(Port.fromInt(config.api.port).getOrElse(port"8080"))
        .withHttpApp(middlewares(httpApp))
        .withErrorHandler(errorHandler)

    server.build
  }

  def run[F[_]: Async: Parallel: Network: Logger](config: ConfigServices): F[Nothing] =
    build[F](config).useForever
}
