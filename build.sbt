organization in ThisBuild := "com.amit"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "7.0.0"

lazy val `spoiler-alert` = (project in file("."))
  .aggregate(`spoiler-alert-api`, `spoiler-alert-impl`)

lazy val `common` = (project in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs,
      scalaTest
    )
  )

lazy val `spoiler-alert-api` = (project in file("spoiler-alert-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`common`)

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
  .dependsOn(`common`,`spoiler-alert-api`)


lagomKafkaEnabled in ThisBuild := false
lagomKafkaAddress in ThisBuild := "localhost:9092"

lagomCassandraEnabled in ThisBuild := false
lagomUnmanagedServices in ThisBuild := Map("cas_native" -> "tcp://localhost:9042")