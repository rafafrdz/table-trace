import sbt.Keys._
import sbt._
import sbt.librarymanagement.Artifact
import sbtassembly.AssemblyKeys.{assembly, assemblyJarName, assemblyMergeStrategy}
import sbtassembly.AssemblyPlugin.autoImport.MergeStrategy
import sbtassembly.PathList

object Assembly {

  lazy val prefix: String     = "table-trace"
  lazy val classifier: String = "with-dependencies"

  def projectSettings: Seq[Setting[_]] =
    Seq(
      assembly / assemblyMergeStrategy := {
        case PathList("META-INF", "MANIFEST.MF")                        => MergeStrategy.discard
        case PathList("META-INF", "native-image", _ @_*)                => MergeStrategy.discard
        case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
        case "module-info.class"                                        => MergeStrategy.discard
        case "META-INF/io.netty.versions.properties"                    => MergeStrategy.first
        case "logback.xml"                                              => MergeStrategy.first
        case x if x.contains("StaticMDCBinder")                         => MergeStrategy.first
        case x if x.endsWith(".html") || x.endsWith(".txt")             => MergeStrategy.discard
        case x => (assembly / assemblyMergeStrategy).value(x)
      },
      // JAR file settings
      assembly / assemblyJarName := s"$prefix-${name.value}-${version.value}.jar"
    )

  def publishAssemblyJar: Seq[Setting[_]] =
    Seq(
      Compile / assembly / artifact := {
        val art: Artifact = (Compile / assembly / artifact).value
        art.withClassifier(Some(classifier))
      }
    ) ++
      addArtifact(Compile / assembly / artifact, assembly)
}
