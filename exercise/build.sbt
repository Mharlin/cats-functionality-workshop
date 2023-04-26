val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "exercise",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test"
    )
  )
