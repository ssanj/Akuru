/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.RegexConstants._

final class MongoCollectionFindSpec extends CommonSpec {

  import Blog._

  "A MongoCollection with find" should "find all saved objects matching a query" in {
    (onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField("sample1"), labelsField(Seq("sample")))) ~~>
              save(Blog(titleField("sample2"), labelsField(Seq("sample")))) ~~>
              save(Blog(titleField("sample3"), labelsField(Seq("sample")))) ~~>
              find[Blog](titleField ?* ("sample*"/)) { all } { blogs =>
                blogs.size should equal (3)
                blogs.exists(_.title.value == "sample1") should be (true)
                blogs.exists(_.title.value == "sample2") should be (true)
                blogs.exists(_.title.value == "sample3") should be (true)
                success
              }
    ) ~~>() verifySuccess
  }

  it should "return zero results if there are no matches" in {
    (onTestDB ~~>
              drop[Blog] ~~>
              find[Blog](titleField ?* ("*"/)) { all }  {blogs =>
                blogs.size should equal (0)
                success
              }
    ) ~~>() verifySuccess
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> find[Person](Person.nameField ?* ("*"/)) { all } (_ => fail("Should not have return results")) ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions throw on creating a query" in {
    (
      onTestDB ~~> find[Blog](createExceptionalMongoObject) { all } (_ => fail("Should not have return results")) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exception thrown by match handler function" in {
    (
      onTestDB ~~> find[Blog](titleField ?* ("*"/)) { all } (_ => throw new RuntimeException("Handler threw an Exception"))  ~~>()
    ) verifyError has ("Handler threw an Exception")
  }

  it should "find regex" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField("Querying with RegEx"), labelsField(Seq("query", "regex")))) ~~>
              find[Blog](titleField ?* ("querying with RegEx"/)) { all } { blogs => { blogs.size should equal (0); success } } ~~>
              find[Blog](titleField ?* ("Querying with RegEx"/i)) { all } { blogs => { blogs.size should equal (1); success } } ~~>
              find[Blog](titleField ?* (".* with RegEx"/)) { all } { blogs => { blogs.size should equal (1); success } } ~~>
              find[Blog](titleField ?* (".*query.*"/i)) { all } { blogs => { blogs.size should equal (1); success } } ~~>()
    ) verifySuccess
  }
}

