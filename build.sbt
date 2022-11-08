lazy val root = project
  .in(file("."))
  .settings(
    name := "sample-grpc-service",
    version := "0.1",
    scalaVersion := "2.13.10"
  )
  .aggregate(protobuf, client, server)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.10",
  organization := "grpc-chatroom",
  version := "0.1",
  libraryDependencies ++= Seq(
    "com.auth0" % "java-jwt" % "4.2.1",
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "com.typesafe" % "config" % "1.4.2",
    "com.github.pureconfig" %% "pureconfig" % "0.17.2",
    "org.scalatest" %% "scalatest" % "3.2.14" % Test
  )
)
lazy val protobuf =
  project
    .in(file("protobuf"))
    .settings(
      commonSettings,
      scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage
    )
    .enablePlugins(Fs2Grpc)

lazy val client =
  project
    .in(file("client"))
    .settings(
      commonSettings
    )
    .dependsOn(protobuf)

lazy val server =
  project
    .in(file("server"))
    .settings(commonSettings)
    .dependsOn(protobuf)
