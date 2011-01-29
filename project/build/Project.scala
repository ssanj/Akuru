import sbt._

class Project(info:ProjectInfo) extends DefaultProject(info) {
  lazy val artifactory = "Artifactory Release" at "http://hyperion:9080/artifactory/libs-releases"
  lazy val mongoDriver = "org.mongodb" % "mongo-java-driver" % "2.4" withSources()
  lazy val scalatest = "org.scalatest" % "scalatest" % "1.2" withSources()
}

