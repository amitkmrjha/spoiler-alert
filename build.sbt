organization in ThisBuild := "com.amit"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `spoiler-alert` = (project in file("."))
  .aggregate(`spoiler-alert-api`, `spoiler-alert-impl`, `spoiler-alert-stream-api`, `spoiler-alert-stream-impl`)

lazy val `spoiler-alert-api` = (project in file("spoiler-alert-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `spoiler-alert-impl` = (project in file("spoiler-alert-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`spoiler-alert-api`)

lazy val `spoiler-alert-stream-api` = (project in file("spoiler-alert-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `spoiler-alert-stream-impl` = (project in file("spoiler-alert-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`spoiler-alert-stream-api`, `spoiler-alert-api`)
