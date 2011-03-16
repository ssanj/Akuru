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
                    ( find (Blog) where (Blog.titleField === "Hello World Lift" and2 Blog.labelsField === Seq("lift", "scala", "sbt"))
                            withResults printBlogs withoutResults noOp ) ~~>
                    ( find (Blog) where (labelsField === {"ubuntu|work"/i} and2 titleField === ("less"/i)) withResults printBlogs withoutResults noOp ) ~~>
                    ( update one Blog where titleField === "lessons learned" withValues set(titleField === "Lessons Learned") returnErrors ) ~~>
                    ( find (Blog) where labelsField === {"work"/i} withResults printBlogs withoutResults  noOp ) ~~>
                    ( update one Blog where titleField === "Lessons Learned" withValues b2 returnErrors ) ~~>
                    ( find (Blog) where labelsField === {"work"/i} withResults printBlogs withoutResults  noOp ) ~~>
                    ( upsert one Blog where titleField === "Semigroup" withValues b3 returnErrors ) ~~>
                    ( find (Blog) where labelsField === {"functional"/} withResults printBlogs withoutResults  noOp ) ~~>
                    ( find (Blog) where labelsField === {".*"/} constrainedBy (Limit(1) and Order(titleField, ASC)) withResults printBlogs withoutResults noOp)
                 } ~~>() getOrElse("success >>")
    println(result)
  }

  def printBlogs(blogs:Seq[Blog]): Option[String] = {
    blogs foreach {blog => println("blog title -> " + blog.title.value + blog.labels.value.mkString("[", ",", "]") ) }
    None
  }

  def withAkuru: FutureConnection = onDatabase("akuru_test")
}