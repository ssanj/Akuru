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
        with DomainObjects
        with MongoFunctions
        with DomainSupport
        with MongoSpecSupport
        with TestDomainObjects {

  "A MongoCollection with find" should "find all saved objects matching a query" in {
    ({
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(title="sample1", labels = Seq("sample"))) ~~>
              save(Blog(title="sample2", labels = Seq("sample"))) ~~>
              save(Blog(title="sample3", labels = Seq("sample"))) ~~>
              find(Blog.title -> ("sample*"/))((blogs:Seq[Blog]) => {
                blogs.size should equal (3)
                blogs.exists(_.title == "sample1") should be (true)
                blogs.exists(_.title == "sample2") should be (true)
                blogs.exists(_.title == "sample3") should be (true)
                success
              })
    } ~~>()) verifySuccess
  }

  it should "return zero results if there are no matches" in {
    ({
      onTestDB ~~>
              drop[Blog] ~~>
              find(Blog.title -> ("*"/)){(blogs:Seq[Blog]) =>
                blogs.size should equal (0)
                success
              }
    } ~~>()) verifySuccess
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> find[Person, MongoObject](Person.name -> ("*"/))(p => fail("Should not have return results")) ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions throw on creating a query" in {
    (
      onTestDB ~~> find[Blog, MongoObject](createExceptionalMongoObject)(b => fail("Should not have return results")) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exception thrown by match handler function" in {
    (
      onTestDB ~~> find(Blog.title -> ("*"/))((blogs:Seq[Blog]) => throw new RuntimeException("Handler threw an Exception")) ~~>()
    ) verifyError has ("Handler threw an Exception")
  }

  it should "find regex" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(title = "Querying with RegEx", labels = Seq("query", "regex"))) ~~>
              find(Blog.title -> ("querying with RegEx"/))((blogs:Seq[Blog]) => { blogs.size should equal (0); success }) ~~>
              find(Blog.title -> ("Querying with RegEx"/i))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) ~~>
              find(Blog.title -> (".* with RegEx"/))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) ~~>
              find(Blog.labels -> (".*query.*"/))((blogs:Seq[Blog]) => { blogs.size should equal (1); success }) ~~>()
    ) verifySuccess
  }
}

