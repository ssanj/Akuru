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
            } withoutResults Failure("Expected 3 got 0"))} withoutResults ((blog, wr) => Failure("Could not save Blog: " + blog.title.value)))}).
    execute verifySuccess
  }

  it should "return zero results if there are no matches" in {
    (drop collection Blog withResults {
      +> (find * Blog where titleField === (".*"/) withResults { b => Failure("Expected 0 but received: " + b.size) } withoutResults Success({}))
    }).execute verifySuccess
  }

  it should "handle exceptions thrown on finder execution" in {
    (drop collection Blog withResults {
      +> (find * Person where Person.nameField === ("*"/) withResults (p => Success(p)) withoutResults Failure("Could not any Person"))
    }).execute verifyFailure (Person.expectedError)
  }

  it should "handle exceptions throw on creating a query" in {
    (drop collection Blog withResults {
      +> (find * Blog where (exceptionalFieldValueJoiner) withResults (b => Success(b)) withoutResults Failure("Did not find Blogs"))
    }).execute verifyFailure (mongoCreationException)
  }

  it should "handle exception thrown by the withResults function" in {
    (drop collection Blog withResults {
      +> (save a Blog withValues (Blog(titleField === "Querying with RegEx", labelsField === Seq("query", "regex"))) withResults {
        +> (find * Blog where titleField === (".*"/) withResults (_ => ex("withResults threw an Exception"))
                withoutResults Failure("Could not find Blog"))
      } withoutResults (wr => Failure("Could not save Blog")))
    }).execute verifyFailure ("withResults threw an Exception")
  }

  it should "handle exception thrown by the withoutResults function" in {
    (drop collection Blog withResults {
      +> (save a Blog withValues (Blog(titleField === "SkidRow", labelsField === Seq("80s", "Rock"))) withResults {
        +> (find * Blog where titleField === ("Blah*"/) withResults (_ => Success({})) withoutResults ex("withoutResults threw an Exception"))
      } withoutResults (wr => Failure("Could not save Blog")))
    }).execute verifyFailure ("withoutResults threw an Exception")
  }

  it should "find regex" in {
    (drop collection Blog withResults {
      +> (save a Blog withValues (Blog(titleField === "Querying with RegEx", labelsField === Seq("query", "regex"))) withResults {
        +> (find * Blog where titleField === ("querying with RegEx"/) withResults (b => Failure("Expected 0 but received: " + b.size))
                withoutResults {
                  +> (find * Blog where titleField === ("querying with regEx"/i) withResults {b1 =>
                    +> (find * Blog where titleField === (".* with RegEx"/) withResults {b2 =>
                     +> (find * Blog where titleField === (".*query.*"/i) withResults {b3 => Success(b3(0).title.value) }
                             withoutResults Failure("Didn't match wilcard with case-insensitive match"))
                    } withoutResults Failure("Didn't match wildcard with case-sensitive match") )
                  } withoutResults Failure("Didn't match case-insensitive match"))
        } )
      } withoutResults(wr => Failure("Could not save Blog")))
    }).execute verifySuccess("Querying with RegEx")
  }

}