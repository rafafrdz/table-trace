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

package io.github.rafafrdz.tabletrace.api.routes

import cats._
import cats.effect.Async
import cats.implicits._
import io.github.rafafrdz.tabletrace.api.domain.errors.APIError
import io.github.rafafrdz.tabletrace.api.handler.codecs._
import io.github.rafafrdz.tabletrace.api.domain.request.AnalyzeQueryRequest
import io.github.rafafrdz.tabletrace.api.handler.AnalyzeQueryHandler
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response, Status}
import org.typelevel.log4cats.Logger

object AnalyzeRoutes {

  private object A {
    val Analyze: String = "analyze"
  }

  def routes[
      F[_]: Async: Parallel: Logger
  ]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case req @ POST -> Root / A.Analyze =>
      for {
        request <- req.as[AnalyzeQueryRequest]
        result  <- AnalyzeQueryHandler.extractTable(request)
        response <- result match {
          case Right(asset) =>
            Ok(asset)
          case Left(APIError(status, message)) =>
            Status
              .fromInt(status.code)
              .fold(
                _ => InternalServerError(message),
                s => Response[F](s).withEntity(message).pure[F]
              )
        }
      } yield response

    }
  }
}
