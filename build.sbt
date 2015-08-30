lazy val baseName = "TreeTable"

lazy val baseNameL = baseName.toLowerCase

name := baseName

lazy val commonSettings = Seq(
  version            := "1.3.8-SNAPSHOT",
  organization       := "de.sciss",
  scalaVersion       := "2.11.7",
  crossScalaVersions := Seq("2.11.7", "2.10.5"),
  javacOptions      ++= Seq("-source", "1.6", "-target", "1.6"),
  description        := "A TreeTable component for Swing",
  homepage           := Some(url(s"https://github.com/Sciss/${name.value}")),
  licenses           := Seq("LGPL v3+" -> url("http://www.gnu.org/licenses/lgpl-3.0.txt"))
) ++ publishSettings

// ---- publishing ----

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    Some(if (isSnapshot.value)
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }
)

lazy val root: Project = Project(id = baseNameL, base = file("."))
  .aggregate(javaProject, scalaProject)
  .dependsOn(javaProject, scalaProject) // i.e. root = full sub project. if you depend on root, will draw all sub modules.
  .settings(commonSettings)
  .settings(
    packagedArtifacts := Map.empty           // prevent publishing anything!
  )

lazy val javaProject = Project(id = s"$baseNameL-java", base = file("java"))
  .settings(commonSettings)
  .settings(
    autoScalaLibrary := false,
    crossPaths       := false,
    javacOptions in Compile ++= Seq("-g", "-target", "1.6", "-source", "1.6"),
    javacOptions in (Compile, doc) := Nil,  // yeah right, sssssuckers
    pomExtra := pomExtraBoth
  )

lazy val scalaProject = Project(id = s"$baseNameL-scala", base = file("scala"))
  .dependsOn(javaProject)
  .settings(commonSettings)
  .settings(
    libraryDependencies += {
      val sv = scalaVersion.value
      if (sv startsWith "2.10")
        "org.scala-lang" % "scala-swing" % sv
      else
        "org.scala-lang.modules" %% "scala-swing" % "1.0.2"
    },
    pomExtra := pomBase ++ pomDevsSciss
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
