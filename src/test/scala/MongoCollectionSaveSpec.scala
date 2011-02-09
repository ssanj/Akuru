/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */


package akuru

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import MongoTypes._
import MongoObject.empty

final class MongoCollectionSaveSpec extends FlatSpec with ShouldMatchers
        with DomainObjects
        with MongoFunctions
        with DomainSupport
        with MongoSpecSupport
        with TestDomainObjects {

  "A MongoCollection" should "save a new MongoObject" in {
     ({ onTestDB ~~>
          drop[Blog] ~~>
          findOne(Blog.title -> "blah") { t:Blog => fail("Shouldn't have found Blog") } { ignoreError } ~~>
          save(Blog(title = "blah", labels = Seq("test", "random"))) ~~>
          findOne(Blog.title -> "blah") { t:Blog =>
              t.title should equal ("blah")
              t.labels should equal (Seq("test", "random"))
              success
          } { fail("Didn't find Blog") }
      } ~~>()) verifySuccess
  }

  it should ("handle errors on object creation") in {

    val expectedError = "ExceptionalBlog threw an Exception on Blog creation!Whoops"

    def exceptionBlog():Blog = throw new RuntimeException(expectedError)
    (onTestDB ~~> (save(exceptionBlog)) ~~>()) verifyError has (expectedError)
  }

  it should ("handle errors that occur during function execution") in {
    (onTestDB ~~> save(Person(name = "sanj")) ~~>()) verifyError has (Person.expectedError)
  }
}