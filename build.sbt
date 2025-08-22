import Dependency.*
import Extension.*

lazy val VersionAPI: String = "0.0.1"

lazy val root =
  (project in file("."))
    .aggregate(middleware, api)
    .disablePlugins(BuildPlugin, AssemblyPlugin, HeaderPlugin)
    .settings(
      name           := "table-trace",
      publish / skip := true
    )

lazy val commonSettings = Seq(
  version        := VersionAPI,
  organization   := "io.github.rafafrdz",
  publish / skip := false,
  libraryDependencies ++= Seq(
    catsEffect.core,
    catsEffect.std,
    http4s.circe,
    http4s.dsl,
    pureconfig.pureconfig,
    http4s.emberServer,
    http4s.emberClient,
    logback.logbackClassic,
    logback.log4catsCore,
    Testing.munit,
    Testing.munitCatsEffect
  )
)

lazy val api =
  (project in file("api"))
    .enablePlugins(AssemblyPlugin)
    .withKindProjector
    .withBetterMonadicFor
    .withAssembly
    .dependsOn(middleware)
    .settings(commonSettings)
    .settings(
      name := "api",
      Compile / run / mainClass := Some("io.github.rafafrdz.tabletrace.api.Main"),
      libraryDependencies ++=
        Seq(
          circe.generic,
          circe.parser,
          circe.genericExtras,
          jsqlparser.jsqlparser
        )
    )

addCommandAlias("run-api", ";project api;runMain io.github.rafafrdz.tabletrace.api.Main")

lazy val middleware = (project in file("middleware"))
  .disablePlugins(AssemblyPlugin)
  .withHeader
  .withKindProjector
  .withBetterMonadicFor
  .settings(commonSettings)
  .settings(
    name := "middleware"
  )

