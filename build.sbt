// Name of the project
name := "pdf-fx-viewer"

// Project version
version := "12.0.2-R18"

// Version of Scala used by the project
scalaVersion := "2.13.1"

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val Http4sVersion =  "0.21.1"
lazy val ZioCatsVersion =  "2.0.0.0-RC12"
lazy val IcePdfVersion = "6.3.2"
lazy val PureConfigVersion = "0.12.2"
lazy val LogbackVersion = "1.2.3"

// Server dependencies
libraryDependencies ++= Seq(
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "dev.zio"         %% "zio-interop-cats"    % ZioCatsVersion,
  "org.icepdf.os" % "icepdf-viewer" % IcePdfVersion,// excludeAll(ExclusionRule("org.bouncycastle")),
  "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add JavaFX dependencies
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m=>
  "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
