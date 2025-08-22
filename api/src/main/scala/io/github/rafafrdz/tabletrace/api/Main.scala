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

import cats.effect.{ExitCode, IO, IOApp}
import io.github.rafafrdz.tabletrace.api.config.ConfigServices
import org.typelevel.log4cats.Logger

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _      <- Logger[IO].debug("Loading API configuration")
      config <- ConfigServices.load.handler[IO]
      _      <- Logger[IO].info("Server starting...")
      _      <- Server.run[IO](config)
    } yield ExitCode.Success
  }
}
