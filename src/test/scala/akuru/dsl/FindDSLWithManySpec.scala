/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package dsl

class FindDSLWithManySpec extends AkuruSpecSupport {
  import Config._
  import Blog._
  "A FindDSL" should "find all saved objects matching a query" in {
    val blogs = Seq(Blog(titleField === "sample1", labelsField === Seq("sample")),
                    Blog(titleField === "sample2", labelsField === Seq("sample")),
                    Blog(titleField === "sample3", labelsField === Seq("sample")))

    (drop collection Blog withResults {
      +> (save * Blog withValues (blogs) withResults {
            +> (find * Blog where titleField === ("sample*"/) constrainedBy (Order(titleField -> ASC)) withResults { blogs =>
              blogs.size should equal (3)
              blogs(0).title.value should equal ("sample1")
              blogs(1).title.value should equal ("sample2")
              blogs(2).title.value should equal ("sample3")
              Success({})
            } withoutResults (Failure("Expected 3 got 0")))} withoutResults ((blog, wr) => Failure("Could not save Blog: " + blog.title.value)))}).
    execute verifySuccess
  }

  it should "return zero results if there are no matches" in {
    (drop collection Blog withResults {
      +> (find * Blog where titleField === (".*"/) withResults { b => Failure("Expected 0 but received: " + b.size) } withoutResults Success({}))
    }).execute verifySuccess
  }
}