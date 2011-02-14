/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package runners

import _root_.akuru._
import MongoTypes._

object AkuruRunner extends TestDomainObjects with MongoFunctions with Tools with SideEffects {


  def main(args: Array[String]) {
    import MongoTypes.RegexConstants._
    import MongoTypes.MongoObject._
    import Blog._

    val b1 = Blog(title = titleField("lessons learned"), labels = labelsField(Seq("jobs", "lessons", "work")))
    val b2 = Blog(title = titleField("lessons learned"), labels = labelsField(Seq("jobs", "work")))
    val b3 = Blog(title = titleField("Semigroup"), labels = labelsField(Seq("functional", "scala", "concepts", "semigroup")))

    val blogs = List(b1,
                     Blog(title = titleField("Hello World Lift"), labels = labelsField(Seq("lift", "scala", "sbt"))),
                     Blog(title = titleField("Linux RAID Failed on Boot"), labels = labelsField(Seq("boot", "degraded", "ubuntu"))))

    val result = {withAkuru ~~>
                    (drop[Blog]) ~~>
                    (drop[Label]) ~~>
                    (blogs.map(b => save[Blog](b))) ~~>
//                    (blogs.flatMap(b => b.labels.map(l => save[Label](Label(value = l)))).toList) ~~>
                    (findOne(Blog.titleField.name -> "Hello World Lift")(printBlog)(ignoreError)) ~~>
                    ( find { (Blog.labelsField.name ->  ("ubuntu|work"/i), "title" -> ("less"/i)) } { printBlogs }) ~~>
                    (update[Blog](Blog.titleField.name -> "lessons learned")(set("title", "Lessons Learned"))) ~~>
                    (findOne { Blog.labelsField.name -> ("work")/i } (printBlog))(ignoreError) ~~>
                    (update[Blog](Blog.titleField.name -> "Lessons Learned")(b2)) ~~>
                    (findOne(Blog.labelsField.name -> ("work")/i)(printBlog))(ignoreError) ~~>
                    (upsert[Blog](Blog.titleField.name -> "Semigroup")(b3)) ~~>
                    (findOne(Blog.labelsField.name -> ("functional"/))(printBlog)(ignoreError))
                 } ~~>() getOrElse("success >>")
    println(result)
  }

  def printBlog(blog:Blog): Option[String] = {
    println("blog title -> " + blog.title.value + blog.labels.value.mkString("[", ",", "]") )
    None
  }
  def printBlogs(blogs:Seq[Blog]): Option[String] = {
    for (blog <- blogs) println(blog)
    None
  }

  def withAkuru: FutureConnection = onDatabase("akuru")
}