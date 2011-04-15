/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package dsl

final class SaveDSLWithMultipleSavesSpec extends AkuruSpecSupport {

  import Config._
  import Blog._

  "A SaveDSL saving *" should "save multiple Domain Objects" in {
    val blogs = Seq(Blog(titleField === "sample1", labelsField === Seq("sample")),
                    Blog(titleField === "sample2", labelsField === Seq("sample")),
                    Blog(titleField === "sample3", labelsField === Seq("sample")))

    (drop collection Blog withResults {
      +> (find * Blog where titleField === ("sample*"/) withResults (blogs => Failure("Expected 0 Blogs but got: " + blogs)) withoutResults {
              +> (save * Blog withValues (blogs) withResults {
                +> (find * Blog where titleField === ("sample*"/) constrainedBy (Order(titleField -> ASC)) withResults { blogs =>
                  blogs.size should equal (3)
                  blogs(0).title.value should equal ("sample1")
                  blogs(1).title.value should equal ("sample2")
                  blogs(2).title.value should equal ("sample3")
                  Success({})
            } withoutResults Failure("Expected 3 got 0"))} withoutResults ((blog, wr) => Failure("Could not save Blog: " + blog.title.value)))})
      }).execute verifySuccess
  }

  it should ("handle Exceptions on object creation") in {
    (save * Blog withValues (Seq(Blog(titleField === "Monads", labelsField === Seq("FP")),
                              exceptionBlog,
                              Blog(titleField === "Monoids", labelsField === Seq("FP"))))
            withResults (Success({})) withoutResults ((blog, _) => Failure("Could not save Blog: " + blog))).
    execute verifyFailure (expectedError)
  }

  it should ("handle Exceptions that occur during save execution") in {
    import Person._
    (save * Person withValues Seq(Person(nameField === "sanj")) withResults(Success({})) withoutResults ((_,_) => Failure("Could not save Person"))).
            execute verifyFailure (Person.expectedError)
  }

  it should ("handle exceptions thrown by the withResults function") in {
    (save * Blog withValues Seq(Blog(titleField === "Misc")) withResults(ex("withResult threw an Exception"))
            withoutResults ((_,_) => Failure("Could not save Person"))).
            execute verifyFailure ("withResult threw an Exception")
  }
}