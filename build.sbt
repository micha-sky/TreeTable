name := "TreeTable"

version in ThisBuild := "1.3.3-SNAPSHOT"

organization in ThisBuild := "de.sciss"

scalaVersion in ThisBuild := "2.10.2"

crossPaths := false

description in ThisBuild := "A TreeTable component for Swing"

homepage in ThisBuild <<= name { n => Some(url("https://github.com/Sciss/" + n)) }

licenses in ThisBuild <<= name { n => Seq("LGPL v2.1+" -> url("https://raw.github.com/Sciss/" + n + "/master/LICENSE")) }

// ---- publishing ----

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild <<= version { v =>
  Some(if (v endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )
}

publishArtifact in Test in ThisBuild := false

pomIncludeRepository in ThisBuild := { _ => false }
