import sbt._
import Keys._

object Build extends sbt.Build {
  lazy val root: Project = Project(
    id            = "treetable",
    base          = file("."),
    aggregate     = Seq(javaProject, scalaProject),
    dependencies  = Seq(javaProject, scalaProject), // i.e. root = full sub project. if you depend on root, will draw all sub modules.
    settings      = Project.defaultSettings ++ Seq(
      publishArtifact in(Compile, packageBin) := false, // there are no binaries
      publishArtifact in(Compile, packageDoc) := false, // there are no javadocs
      publishArtifact in(Compile, packageSrc) := false, // there are no sources
      autoScalaLibrary := false,
      pomExtra := pomExtraBoth
    )
  )

  lazy val javaProject = Project(
    id = "treetable-java",
    base = file("java"),
    settings = Project.defaultSettings ++ Seq(
      autoScalaLibrary := false,
      crossPaths := false,
      // javacOptions += "-g",  // generate all debugging symbols
      pomExtra := pomExtraBoth
    )
  )

  lazy val scalaProject = Project(
    id = "treetable-scala",
    base = file("scala"),
    dependencies = Seq(javaProject),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies <+= scalaVersion { sv =>
        "org.scala-lang" % "scala-swing" % sv
      },
      pomExtra := pomBase ++ pomDevsSciss
    )
  )

  def pomExtraBoth = pomBase ++ pomDevsBoth

  def pomBase =
    <scm>
      <url>git@github.com:Sciss/TreeTable.git</url>
      <connection>scm:git:git@github.com:Sciss/TreeTable.git</connection>
    </scm>

  def pomDevSciss =
    <developer>
      <id>sciss</id>
      <name>Hanns Holger Rutz</name>
      <url>http://www.sciss.de</url>
    </developer>

  def pomDevAephyr =
    <developer>
      <id>aephyr</id>
      <name>unknown</name>
      <url>http://code.google.com/p/aephyr/</url>
    </developer>

  def pomDevsBoth =
    <developers>
      {pomDevSciss}
      {pomDevAephyr}
    </developers>

  def pomDevsSciss =
    <developers>
      {pomDevSciss}
    </developers>
}
