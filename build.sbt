lazy val root = project
  .in(file("."))
  .settings(
    name := "sample-grpc-service",
    version := "0.1",
    scalaVersion := "2.12.13"
  )
  .aggregate(protobuf, client, server)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.3",
  organization := "grpc-chatroom",
  version := "0.1",
  libraryDependencies ++= Seq(
    "com.auth0" % "java-jwt" % "3.18.3",
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    "com.typesafe" % "config" % "1.4.1",
    "com.github.pureconfig" %% "pureconfig" % "0.17.1"
  )
)
lazy val protobuf =
  project
    .in(file("protobuf"))
    .settings(
      commonSettings,
      //scalapbCodeGeneratorOptions += CodeGeneratorOption.FlatPackage,
      Compile / PB.targets ++= Seq(
        scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
      )
    )
    .enablePlugins(Fs2Grpc)

lazy val `zio-grpc-app` =
  project
    .in(file("zio-grpc-app"))
    .settings(
      commonSettings
    )
    .dependsOn(protobuf)

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
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
      ),
      commonSettings
    )
    .dependsOn(protobuf)
