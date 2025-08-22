import de.heikoseeberger.sbtheader.HeaderPlugin
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
object BuildPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin && HeaderPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    organizationName         := "table-trace",
    organization             := "io.github.rafafrdz",
    scalaVersion             := Version.Scala,
    crossScalaVersions       := Vector(scalaVersion.value),
    publish / skip           := true,
    run / fork               := true,
    Test / fork              := true,
    Test / parallelExecution := true,
    Test / scalacOptions     := Seq("-Ymacro-annotations"),
    // scalafmtOnCompile        := true,
    updateOptions := updateOptions.value
      .withCachedResolution(cachedResolution = false),
    // do not build and publish scaladocs
    Compile / doc / sources                := Seq.empty,
    Compile / packageDoc / publishArtifact := false,
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    )
  ) ++ Header.projectSettings

}
