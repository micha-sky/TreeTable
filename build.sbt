name := "JTreeTable"

version := "1.2.1"

organization := "de.sciss"

// scalaVersion := "2.10.1"

autoScalaLibrary := false

crossPaths := false

description := "A TreeTable component for Swing"

homepage <<= name { n => Some(url("https://github.com/Sciss/" + n)) }

licenses <<= name { n => Seq("BSD-style" -> url("https://raw.github.com/Sciss/" + n + "/master/LICENSE")) }

// ---- publishing ----

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  Some(if (v endsWith "-SNAPSHOT")
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra <<= name { n =>
<scm>
  <url>git@github.com:Sciss/{n}.git</url>
  <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
  <developer>
    <id>aephyr</id>
    <name>unknown</name>
    <url>http://code.google.com/p/aephyr/</url>
  </developer>
</developers>
}

