name := "todobackend-endpoints4s"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.endpoints4s" %% "algebra" % "1.1.0",
//  "org.endpoints4s" %% "json-schema-generic" % "1.1.0",
  "org.endpoints4s" %% "http4s-server" % "2.0.0",
  "org.http4s" %% "http4s-blaze-server" % "0.21.5",
  "org.tpolecat" %% "doobie-hikari" % "0.9.0",
  "org.tpolecat" %% "doobie-postgres" % "0.9.0",
  "org.tpolecat" %% "doobie-quill" % "0.9.0",
  "org.webjars" % "swagger-ui" % "3.31.1",
  "org.slf4j" % "slf4j-simple" % "1.7.30"
)

enablePlugins(JavaAppPackaging)
