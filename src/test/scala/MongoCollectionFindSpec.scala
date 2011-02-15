/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import MongoTypes._
import MongoTypes.RegexConstants._

final class MongoCollectionFindSpec extends FlatSpec with ShouldMatchers
        with TestDomainObjects
        with MongoFunctions
        with MongoSpecSupport
        with Tools {

  import Blog._

  "A MongoCollection with find" should "find all saved objects matching a query" in {
    ({
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(title = titleField("sample1"), labels = labelsField(Seq("sample")))) ~~>
              save(Blog(title= titleField("sample2"), labels = labelsField(Seq("sample")))) ~~>
              save(Blog(title = titleField("sample3"), labels = labelsField(Seq("sample")))) ~~>
              find(Blog.titleField -> ("sample*"/))((blogs:Seq[Blog]) => {
                blogs.size should equal (3)
                blogs.exists(_.title.value == "sample1") should be (true)
                blogs.exists(_.title.value == "sample2") should be (true)
                blogs.exists(_.title.value == "sample3") should be (true)
                success
              })
    } ~~>()) verifySuccess
  }

  it should "return zero results if there are no matches" in {
    ({
      onTestDB ~~>
              drop[Blog] ~~>
              find(Blog.titleField -> ("*"/)){(blogs:Seq[Blog]) =>
                blogs.size should equal (0)
                success
              }
    } ~~>()) verifySuccess
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> find[Person](Person.nameField -> ("*"/))(p => fail("Should not have return results")) ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions throw on creating a query" in {
    (
      onTestDB ~~> find[Blog](createExceptionalMongoObject)(b => fail("Should not have return results")) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exception thrown by match handler function" in {
    (
      onTestDB ~~> find(Blog.titleField -> ("*"/))((blogs:Seq[Blog]) => throw new RuntimeException("Handler threw an Exception")) ~~>()
    ) verifyError has ("Handler threw an Exception")
  }

  it should "find regex" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(title = titleField("Querying with RegEx"), labels = labelsField(Seq("query", "regex")))) ~~>
              find(Blog.titleField -> ("querying with RegEx"/))((blogs:Seq[Blog]) => { blogs.size should equal (0); success }) ~~>
              find(Blog.titleField -> ("Querying with RegEx"/i))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) ~~>
              find(Blog.titleField -> (".* with RegEx"/))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) ~~>
              find(Blog.titleField -> (".*query.*"/i))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) ~~>()
    ) verifySuccess
  }
}

