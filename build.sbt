name := "schema-validator"

version := "0.1"

scalaVersion := "2.13.8"

val AkkaVersion     = "2.6.18"
val AkkaHTTPVersion = "10.2.6"
val circeVersion    = "0.14.1"

val deps = Seq(
  "com.github.java-json-tools"  % "json-schema-validator" % "2.2.14",
  "org.postgresql"              % "postgresql"            % "42.3.1",
  "ch.qos.logback"              % "logback-classic"       % "1.2.10",
  "com.typesafe.scala-logging" %% "scala-logging"         % "3.9.4",
  "de.heikoseeberger"          %% "akka-http-circe"       % "1.39.2",
  "com.typesafe.akka"          %% "akka-actor"            % AkkaVersion,
  "com.typesafe.akka"          %% "akka-stream"           % AkkaVersion,
  "com.typesafe.akka"          %% "akka-http"             % AkkaHTTPVersion,
  "com.typesafe.akka"          %% "akka-http-testkit"     % "10.2.6"   % Test,
  "org.scalatest"              %% "scalatest"             % "3.2.9"    % Test,
  "org.scalatestplus"          %% "mockito-3-4"           % "3.2.10.0" % Test
)

lazy val circe = Seq(
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion
)

libraryDependencies ++= deps ++ circe

assembly / assemblyMergeStrategy := {
  case PathList("reference.conf")    => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}
