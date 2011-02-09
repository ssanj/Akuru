/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.query

object AkuruMain extends DomainObjects with Tools with SideEffects with MongoFunctions with DomainSupport {


  def main(args: Array[String]) {
    import RegexConstants._
    import MongoObject._

    val b1 = Blog(title = "lessons learned", labels = Seq("jobs", "lessons", "work"))
    val b2 = Blog(title = "lessons learned", labels = Seq("jobs", "work"))
    val b3 = Blog(title = "Semigroup", labels = Seq("functional", "scala", "concepts", "semigroup"))

    val blogs = List(b1,
                     Blog(title = "Hello World Lift", labels = Seq("lift", "scala", "sbt")),
                     Blog(title = "Linux RAID Failed on Boot", labels = Seq("boot", "degraded", "ubuntu")))

    val result = {withAkuru ~~>
                    (drop[Blog]) ~~>
                    (drop[Label]) ~~>
                    (blogs.map(b => save(b))) ~~>
                    (blogs.flatMap(b => b.labels.map(l => save(Label(value = l)))).toList) ~~>
                    (findOne(Blog.title -> "Hello World Lift")(printBlog)(ignoreError)) ~~>
                    ( find { (Blog.labels ->  ("ubuntu|work"/i), "title" -> ("less"/i)) } { printBlogs }) ~~>
                    (update[Blog](Blog.title -> "lessons learned")(set("title", "Lessons Learned"))) ~~>
                    (findOne { Blog.labels -> ("work")/i } (printBlog))(ignoreError) ~~>
                    (update[Blog](Blog.title -> "Lessons Learned")(b2)) ~~>
                    (findOne(Blog.labels -> ("work")/i)(printBlog))(ignoreError) ~~>
                    (upsert[Blog](Blog.title -> "Semigroup")(b3)) ~~>
                    (findOne(Blog.labels -> ("functional"/))(printBlog)(ignoreError))
                 } ~~>() getOrElse("success >>")
    println(result)
  }

  def printBlog(blog:Blog): Option[String] = {
    println("blog title -> " + blog.title + blog.labels.mkString("[", ",", "]") )
    None
  }
  def printBlogs(blogs:Seq[Blog]): Option[String] = {
    for (blog <- blogs) println(blog)
    None
  }

  def withAkuru: FutureConnection = onDatabase("akuru")
}