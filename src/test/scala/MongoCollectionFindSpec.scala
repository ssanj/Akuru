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
              save(Blog(title = titleField("sample1"), labels = labelsField(Seq("sample")))) ~~>
              save(Blog(title= titleField("sample2"), labels = labelsField(Seq("sample")))) ~~>
              save(Blog(title = titleField("sample3"), labels = labelsField(Seq("sample")))) ~~>
              find(titleField ?* ("sample*"/))((blogs:Seq[Blog]) => {
                blogs.size should equal (3)
                blogs.exists(_.title.value == "sample1") should be (true)
                blogs.exists(_.title.value == "sample2") should be (true)
                blogs.exists(_.title.value == "sample3") should be (true)
                success
              }) { full }
    ) ~~>() verifySuccess
  }

  it should "return zero results if there are no matches" in {
    (onTestDB ~~>
              drop[Blog] ~~>
              find(titleField ?* ("*"/)){(blogs:Seq[Blog]) =>
                blogs.size should equal (0)
                success
              } { full }
    ) ~~>() verifySuccess
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> find[Person](Person.nameField ?* ("*"/))(p => fail("Should not have return results")) (full)~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions throw on creating a query" in {
    (
      onTestDB ~~> find[Blog](createExceptionalMongoObject)(b => fail("Should not have return results")) (full) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exception thrown by match handler function" in {
    (
      onTestDB ~~> find(titleField ?* ("*"/))((blogs:Seq[Blog]) => throw new RuntimeException("Handler threw an Exception")) (full) ~~>()
    ) verifyError has ("Handler threw an Exception")
  }

  it should "find regex" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(title = titleField("Querying with RegEx"), labels = labelsField(Seq("query", "regex")))) ~~>
              find(titleField ?* ("querying with RegEx"/))((blogs:Seq[Blog]) => { blogs.size should equal (0); success }) (full) ~~>
              find(titleField ?* ("Querying with RegEx"/i))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) (full)~~>
              find(titleField ?* (".* with RegEx"/))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) (full) ~~>
              find(titleField ?* (".*query.*"/i))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) (full) ~~>()
    ) verifySuccess
  }
}

