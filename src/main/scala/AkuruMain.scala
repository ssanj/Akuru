/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.query

object AkuruMain extends DomainObjects with Tools with SideEffects with MongoFunctions with DomainSupport {


  def main(args: Array[String]) {
    val blogs = List(Blog(title = "lessons learned", labels = Seq("jobs", "lessons", "work")),
                     Blog(title = "Hello World Lift", labels = Seq("lift", "scala", "sbt")),
                     Blog(title = "Linux RAID Failed on Boot", labels = Seq("boot", "degraded", "ubuntu")))

    val result = {withAkuru ~~>
                    (blogs.map(b => save(b) _)) ~~> (blogs.flatMap(b => b.labels.map(l => save(Label(value = l)) _)).toList) ~~>
                    (findOne(query("title" -> "Hello World Lift"))(printBlog) _)
                 } ~~>() getOrElse("success >>")
    println(result)
  }

  def printBlog(blog:Blog): Option[String] = {
    println("blog title -> " + blog.title + blog.labels.mkString("[", ",", "]") )
    None
  }

  def withAkuru: FutureConnection = onDatabase("akuru")
}