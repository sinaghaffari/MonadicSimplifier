name := "MonadicSimplifier"

version := "0.1"
organization := "com.sinaghaffari"

scalaVersion := "3.1.0"
crossScalaVersions := Seq("3.1.0", "2.13.7", "2.12.15", "2.11.12")
credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credential")
description := "Converts and wraps many Scala monads to simplify for-yield workflows."

homepage := Some(url("https://github.com/sinaghaffari/MonadicSimplifier"))
licenses += (
  "GNU LESSER GENERAL PUBLIC LICENSE",
  url("https://github.com/sinaghaffari/MonadicSimplifier/blob/master/LICENSE")
)
scmInfo := Some(
  ScmInfo(
    url("https://github.com/sinaghaffari/MonadicSimplifier"),
    "git@github.com:sinaghaffari/MonadicSimplifier.git"
  )
)
developers := List(
  Developer("sinaghaffari", "Sina Ghaffari", "@sinaghaffari", url("https://github.com/sinaghaffari"))
)

publishTo := sonatypePublishToBundle.value