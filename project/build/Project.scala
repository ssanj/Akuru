import sbt._

class Project(info:ProjectInfo) extends DefaultProject(info) {
  lazy val artifactory = "Artifactory Release" at "http://hyperion:9080/artifactory/libs-releases"
  lazy val mongoDriver = "org.mongodb" % "mongo-java-driver" % "2.5.2" withSources()
  lazy val scalatest = "org.scalatest" % "scalatest" % "1.2" withSources()
  lazy val borachio = "com.borachio" %% "borachio" % "latest.integration"

  override def compileOptions = CompileOption("-unchecked") :: CompileOption("-encoding") :: CompileOption("UTF-8") :: super.compileOptions.toList
}

