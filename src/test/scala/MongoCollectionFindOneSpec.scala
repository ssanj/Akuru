/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import MongoTypes._
import MongoTypes.RegexConstants._

final class MongoCollectionFindOneSpec extends FlatSpec with ShouldMatchers
        with DomainObjects
        with MongoFunctions
        with DomainSupport
        with MongoSpecSupport
        with TestDomainObjects {

  "A MongoCollection with findOne" should "find a single match even if multiple matches exist" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(title = "blah1")) ~~> save(Blog(title = "blah2")) ~~> save(Blog(title = "blah3")) ~~>
            findOne(Blog.title -> ("blah.*"/i)) { blog:Blog =>
                blog.title should include regex ("blah")
                success
            } { fail("Should have found hits") }
    ) ~~>() verifySuccess
  }

  it  should "call a nomatches handler function if it does not have any matches" in {
    var handlerCalled = false
    (
      onTestDB ~~> drop[Blog] ~~> findOne(Blog.title -> ("blah"/)) { b:Blog => Some("Unexpected blog returned -> " + b) }
                { handlerCalled = true } ~~>()
    ) verifySuccess()

    handlerCalled should be (true)
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> findOne(Person.name -> ("*"/)){ p:Person => Some("Should not have return results") } { ignoreError } ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions thrown on creating a query" in {
    (
      onTestDB ~~> findOne(createExceptionalMongoObject) { b:Blog => Some("Should not have return results") }
                { throw new RuntimeException("Handler should not be called on error") } ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exceptions thrown by match handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(title = "blah1")) ~~>
              findOne(Blog.title -> (".*"/)) { b:Blog => throw new RuntimeException("Exception thrown handling match") }
                        { throw new RuntimeException("Handler should not be called on error") } ~~>()
    ) verifyError has ("Exception thrown handling match")
  }

  it should "handle exceptions thrown by nomatches handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              findOne(Blog.title -> (".*"/)) { b:Blog => Some("Should not have return results -> " + b) }
                        { throw new RuntimeException("Handler function threw an Exception") } ~~>()
    ) verifyError has ("Handler function threw an Exception")
  }
}
