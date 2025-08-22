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

package io.github.rafafrdz.tabletrace.api.handler

import cats.MonadThrow
import cats.implicits._
import io.github.rafafrdz.tabletrace.api.Result
import io.github.rafafrdz.tabletrace.api.domain.errors.APIError
import io.github.rafafrdz.tabletrace.api.domain.request.AnalyzeQueryRequest
import io.github.rafafrdz.tabletrace.api.domain.response.AnalyzeQueryExtractTableNamesResponse
import net.sf.jsqlparser.util.TablesNamesFinder
import org.http4s.Status
import org.typelevel.log4cats.Logger

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object AnalyzeQueryHandler {

  def extractTable[F[_]: Logger: MonadThrow](
      request: AnalyzeQueryRequest
  ): F[Result[AnalyzeQueryExtractTableNamesResponse]] =
    Try(
      TablesNamesFinder.findTables(request.query)
    ) match {
      case Failure(exception) =>
        Logger[F].error(exception)("Error processing the query") *> APIError(
          Status.UnprocessableEntity,
          s"Error processing the query `${request.query}`"
        ).asLeft[AnalyzeQueryExtractTableNamesResponse].pure[F]
      case Success(value) =>
        AnalyzeQueryExtractTableNamesResponse(value.asScala.toSet).asRight[APIError].pure[F]
    }

}
