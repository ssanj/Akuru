/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package runners

import _root_.akuru._

object AkuruRunner extends TestDomainObjects with AkuruDSL {


  def main(args: Array[String]) {
    import MongoTypes.MongoObject.set
    import Blog._
    import Label._

    val b1 = Blog(titleField === "lessons learned", labelsField === Seq("jobs", "lessons", "work"))
    val b2 = Blog(titleField === "lessons learned", labelsField === Seq("jobs", "work"))
    val b3 = Blog(titleField === "Semigroup", labelsField === Seq("functional", "scala", "concepts", "semigroup"))

    val blogs = List(b1,
                     Blog(titleField === "Hello World Lift", labelsField === Seq("lift", "scala", "sbt")),
                     Blog(titleField === "Linux RAID Failed on Boot", labelsField === Seq("boot", "degraded", "ubuntu")))

    val result = {withAkuru ~~>
                    drop[Blog] ~~>
                    drop[Label] ~~>
                    blogs.map(b => save(b)) ~~>
                    (blogs.flatMap(b => b.labels.value.map(l => save(Label(valueField === l)))).toList) ~~>
                    ( find one Blog where (Blog.titleField === "Hello World Lift" and2 Blog.labelsField === Seq("lift", "scala", "sbt"))
                            withResults printBlog onError noOp ) ~~>
                    ( find many Blog where (labelsField ?* ("ubuntu|work"/i) and titleField ?* ("less"/i)) withResults printBlogs ) ~~>
                    ( update one Blog where titleField === "lessons learned" withValues set(titleField === "Lessons Learned") returnErrors ) ~~>
                    ( find one Blog where labelsField ?* ("work"/i) withResults printBlog onError noOp ) ~~>
                    ( update one Blog where titleField === "Lessons Learned" withValues b2 returnErrors ) ~~>
                    ( find one Blog where labelsField ?* ("work"/i) withResults printBlog onError noOp ) ~~>
                    upsert[Blog]{ titleField === "Semigroup" } { b3 } ~~>
                    ( find one Blog where labelsField ?* ("functional"/) withResults printBlog onError noOp ) ~~>
                    ( find many Blog where labelsField ?* (".*"/) constrainedBy (Limit(1) and Order(titleField, ASC)) withResults printBlogs )
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