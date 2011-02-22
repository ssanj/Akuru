/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package runners

import _root_.akuru._
import MongoTypes._

object AkuruRunner extends TestDomainObjects {


  def main(args: Array[String]) {
    import MongoTypes.MongoObject.set
    import Blog._
    import Label._

    val b1 = Blog(title = titleField("lessons learned"), labels = labelsField(Seq("jobs", "lessons", "work")))
    val b2 = Blog(title = titleField("lessons learned"), labels = labelsField(Seq("jobs", "work")))
    val b3 = Blog(title = titleField("Semigroup"), labels = labelsField(Seq("functional", "scala", "concepts", "semigroup")))

    val blogs = List(b1,
                     Blog(title = titleField("Hello World Lift"), labels = labelsField(Seq("lift", "scala", "sbt"))),
                     Blog(title = titleField("Linux RAID Failed on Boot"), labels = labelsField(Seq("boot", "degraded", "ubuntu"))))

    val result = {withAkuru ~~>
                    drop[Blog] ~~>
                    drop[Label] ~~>
                    blogs.map(b => save(b)) ~~>
                    (blogs.flatMap(b => b.labels.value.map(l => save(Label(value = valueField(l))))).toList) ~~>
                    findOne(Blog.titleField("Hello World Lift"))(printBlog)(ignoreError)~~>
                    find { (Blog.labelsField.name ->  ("ubuntu|work"/i), Blog.titleField.name -> ("less"/i)) } { printBlogs } { full } ~~>
                    update[Blog](Blog.titleField("lessons learned"))(set(Blog.titleField("Lessons Learned"))) ~~>
                    findOne { Blog.labelsField.name -> ("work")/i } (printBlog)(ignoreError) ~~>
                    update[Blog](Blog.titleField("Lessons Learned"))(b2) ~~>
                    findOne(Blog.labelsField -> ("work")/i)(printBlog)(ignoreError) ~~>
                    upsert[Blog](Blog.titleField("Semigroup"))(b3) ~~>
                    findOne(Blog.labelsField -> ("functional"/))(printBlog)(ignoreError)
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