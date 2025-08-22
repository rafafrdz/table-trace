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

package io.github.rafafrdz.tabletrace.middleware.cors

import cats.Applicative
import org.http4s.HttpApp
import org.http4s.server.middleware.CORS

object CorsMiddleware {

  /**
   * Simple CORS middleware configuration.
   *
   * This implementation uses a permissive CORS policy (`withAllowOriginAll`) to allow requests from all origins.
   * All HTTP methods are accepted (`withAllowMethodsAll`), and credentials (cookies, Authorization headers)
   * are optionally allowed based on the `security` flag.
   *
   * The `withMaxAgeDefault` sets a default max-age for preflight request caching.
   *
   * NOTE:
   *   - In production environments, `withAllowOriginAll` should be replaced by a specific origin whitelist.
   *   - Access control by token or header validation should be implemented in a separate middleware layer.
   *
   * @param httpApp  The underlying HTTP application.
   * @param security If true, allows credentials to be included in CORS requests.
   * @return A CORS-wrapped HttpApp with permissive or semi-restricted behavior.
   */
  def impl[F[_]: Applicative](httpApp: HttpApp[F], security: Boolean = false): HttpApp[F] =
    CORS.policy.withAllowOriginAll.withAllowMethodsAll
      .withAllowCredentials(security)
      .withMaxAgeDefault
      .apply(httpApp)

}
