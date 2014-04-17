name := "TreeTable"

version in ThisBuild            := "1.3.5"

organization in ThisBuild       := "de.sciss"

scalaVersion in ThisBuild       := "2.11.0"

crossScalaVersions in ThisBuild := Seq("2.11.0", "2.10.4")

javacOptions in ThisBuild      ++= Seq("-source", "1.6", "-target", "1.6")

crossPaths := false

description in ThisBuild        := "A TreeTable component for Swing"

homepage in ThisBuild           := Some(url("https://github.com/Sciss/" + name.value))

licenses in ThisBuild           := Seq("LGPL v3+" -> url("http://www.gnu.org/licenses/lgpl-3.0.txt"))

// ---- publishing ----

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild :=
  Some(if (version.value endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test in ThisBuild := false

pomIncludeRepository in ThisBuild := { _ => false }
