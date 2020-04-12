// Name of the project
name := "pdf-elm-viewer"

// Project version
version := "0.0.1"

// Version of Scala used by the project
scalaVersion := "2.13.1"

lazy val Http4sVersion =  "0.21.1"
lazy val CirceVersion = "0.13.0"
lazy val ZioCatsVersion =  "2.0.0.0-RC12"
lazy val PureConfigVersion = "0.12.2"
lazy val LogbackVersion = "1.2.3"

// Server dependencies
libraryDependencies ++= Seq(
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "io.circe"        %% "circe-generic"       % CirceVersion,
  "dev.zio"         %% "zio-interop-cats"    % ZioCatsVersion,
  "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

enablePlugins(SbtWeb)

// TODO: This doesn't work the way i want it.
import ElmKeys._
elmOutput in elmMake := (resourceDirectory in Compile).value / "js" / "elmMain.js"
(compile in Compile) := ((compile in Compile) dependsOn elmMake).value

