/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package runners

import _root_.akuru._
import MongoTypes.MongoObject.$set

object AkuruRunner extends TestDomainObjects with AkuruDSL {

  def main(args: Array[String]) {
    import Blog._
    import Label._
    import DailySpend._
    import DailySpend.Spend._
    import DailySpend.Spend.Tag._

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
                    ( find * Blog where (titleField === "Hello World Lift" and labelsField === Seq("lift", "scala", "sbt"))
                            withResults printBlogs withoutResults noOp ) ~~>
                    ( find * Blog where (labelsField === {"ubuntu|work"/i} and titleField === ("less"/i)) withResults printBlogs withoutResults noOp ) ~~>
                    ( update a Blog where titleField === "lessons learned" withValues $set(titleField === "Lessons Learned") returnErrors ) ~~>
                    ( find * Blog where labelsField === {"work"/i} withResults printBlogs withoutResults  noOp ) ~~>
                    ( update a Blog where titleField === "Lessons Learned" withValues b2 returnErrors ) ~~>
                    ( find * Blog where labelsField === {"work"/i} withResults printBlogs withoutResults  noOp ) ~~>
                    ( upsert a Blog where titleField === "Semigroup" withValues b3 returnErrors ) ~~>
                    ( find * Blog where labelsField === {"functional"/} withResults printBlogs withoutResults  noOp ) ~~>
                    ( find * Blog where labelsField === {".*"/} constrainedBy (Limit(1) and Order(titleField -> ASC)) withResults printBlogs withoutResults noOp) ~~>
                    drop[DailySpend] ~~>
                    save(DailySpend(dateField === 123456L,
                                    spendsField ===
                                            Spend(costField === 12.23D, descriptionField === "blah",
                                              tagsField === Seq(Tag(nameField === "tag1"), Tag(nameField ==="tag2"))))) ~~>
                    ( find * DailySpend where (dateField === 123456L) withResults (printDS) withoutResults showError("got nothing") )
                 } ~~>() getOrElse("success >>")
    println(result)
  }

  def printBlogs(blogs:Seq[Blog]): Option[String] = {
    blogs foreach {blog => println("blog title -> " + blog.title.value + ", labels -> " + blog.labels.value.mkString("[", ",", "]") ) }
    None
  }

  def printDS(dses:Seq[DailySpend]): Option[String] = {

    import DailySpend._
    import DailySpend.Spend._

    def tagString(tag:Tag): String = tag.name.value

    def spendString(spend:Spend): String = {
      "cost -> " + spend.cost.value + ", description -> " + spend.description.value + ", tags: " +
              spend.tags.value.map(tag => tagString(tag)).mkString("[", ",", "]")
    }

    dses foreach (ds => println("DailySpend { date -> " + ds.date.value + ", spend { " + spendString(ds.spends.value) + " } }"))
    None
  }

  def withAkuru: FutureConnection = onDatabase("akuru_test")

  def showError(msg:String) : Option[String] = Some(msg)
}