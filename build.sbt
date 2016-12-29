lazy val baseName = "TreeTable"
lazy val baseNameL = baseName.toLowerCase

lazy val projectVersion = "1.3.9"
lazy val mimaVersion    = "1.3.5"

name := baseName

// ---- scala main dependencies ----

lazy val swingPlusVersion = "0.2.2"

// ---- test dependencies ----

lazy val subminVersion = "0.2.1"

def basicJavaOpts = Seq("-source", "1.6")

lazy val commonSettings = Seq(
  version            := projectVersion,
  organization       := "de.sciss",
  scalaVersion       := "2.11.8",
  crossScalaVersions := Seq("2.12.1", "2.11.8", "2.10.6"),
  javacOptions                   := basicJavaOpts ++ Seq("-encoding", "utf8", "-Xlint:unchecked", "-target", "1.6"),
  javacOptions in (Compile, doc) := basicJavaOpts,  // doesn't eat `-encoding` or `target`
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

lazy val root = Project(id = baseNameL, base = file("."))
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
    pomExtra := pomExtraBoth,
    mimaPreviousArtifacts := Set("de.sciss" % s"$baseNameL-java" % mimaVersion)
  )

lazy val scalaProject = Project(id = s"$baseNameL-scala", base = file("scala"))
  .dependsOn(javaProject)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "de.sciss" %% "swingplus" % swingPlusVersion,
      "de.sciss" %  "submin"    % subminVersion % "test"
    ),
    pomExtra := pomBase ++ pomDevsSciss,
    mimaPreviousArtifacts := Set("de.sciss" %% s"$baseNameL-scala" % mimaVersion)
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
