/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package dsl

final class SaveDSLSpec extends AkuruSpecSupport {

  import Config._
  import Blog._

  "A SaveDSL" should "save a DomainObject" in {
    (drop collection Blog withResults {
      +> (find * Blog where titleField === "blah" withResults (blogs => Failure("Shouldn't have found Blogs: " + blogs)) withoutResults {
        +> (save a Blog withValues (Blog(titleField === "blah", labelsField === Seq("test", "random"))) withResults {
          +> (find * Blog where titleField === "blah" withResults {blogs =>
            blogs.size should equal (1)
            blogs(0).title.value should equal ("blah")
            blogs(0).labels.value should equal (Seq("test", "random"))
            Success({})
          } withoutResults Failure("Did not find Blog"))
        } withoutResults(_ => Failure("Could not save Blog")))
      })
    }).execute verifySuccess
  }

  it should ("handle Exceptions on object creation") in {
    (save a Blog withValues (exceptionBlog) withResults (Success({})) withoutResults (_ => Failure("Could not save Blog"))).
            execute verifyFailure (expectedError)
  }

  it should ("handle Exceptions that occur during save execution") in {
    import Person._
    (save a Person withValues Person(nameField === "sanj") withResults(Success({})) withoutResults (_ => Failure("Could not save Person"))).
            execute verifyFailure (Person.expectedError)
  }

  it should ("handle exceptions thrown by the withResults function") in {
    (save a Blog withValues Blog(titleField === "Misc") withResults(ex("withResult threw an Exception"))
            withoutResults (_ => Failure("Could not save Person"))).
            execute verifyFailure ("withResult threw an Exception")
  }
}