
name := "core"

version:= "1.0.0"

libraryDependencies ++= Seq(
  "com.mandrillapp.wrapper.lutung" % "lutung" % "0.0.8",
  "org.ehcache" % "ehcache" % "3.4.0",
  "org.reflections" % "reflections" % "0.9.11",
  "io.swagger" %% "swagger-play2" % "1.6.0",
  "org.quartz-scheduler" % "quartz" % "2.3.1",
  "io.netty" % "netty-buffer" % "4.1.17.Final",
  "io.netty" % "netty-transport" % "4.1.17.Final",
  "io.netty" % "netty-handler" % "4.1.17.Final",
  "org.mongodb" % "mongodb-driver-reactivestreams" % "1.10.0",
  "org.mongodb" % "mongodb-driver" % "3.9.1",
  "xyz.morphia.morphia" % "core" % "1.4.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "io.sentry" % "sentry" % "1.7.16",
  "software.amazon.awssdk" % "s3" % "2.7.1",
  "com.sendgrid" % "sendgrid-java" % "4.4.1",
  javaForms,
  javaWs,
  guice
)

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false