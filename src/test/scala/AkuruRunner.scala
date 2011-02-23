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
                    findOne{ titleField("Hello World Lift") } { printBlog } { noOp } ~~>
                    find { (labelsField ?* ("ubuntu|work"/i) and titleField ?* ("less"/i)) } { printBlogs } { full } ~~>
                    update[Blog]{ titleField("lessons learned") } { set(titleField("Lessons Learned")) } ~~>
                    findOne { labelsField ?* ("work"/i) } { printBlog } { noOp } ~~>
                    update[Blog]{ titleField("Lessons Learned") } { b2 } ~~>
                    findOne{ labelsField ?* ("work"/i) } { printBlog } { noOp } ~~>
                    upsert[Blog]{ titleField("Semigroup") } { b3 } ~~>
                    findOne{ labelsField ?* ("functional"/) } { printBlog } { noOp }
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

  def withAkuru: FutureConnection = onDatabase("akuru_test")
}