/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import MongoTypes._
import MongoTypes.RegexConstants._

final class MongoCollectionFindOneSpec extends CommonSpec {

  import Blog._

  "A MongoCollection with findOne" should "find a single match even if multiple matches exist" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(titleField("blah1"))) ~~> save(Blog(titleField("blah2"))) ~~> save(Blog(titleField("blah3"))) ~~>
            findOne[Blog](titleField ?* ("blah.*"/i)) { blog =>
                blog.title.value should include regex ("blah")
                success
            } { fail("Should have found hits") }
    ) ~~>() verifySuccess
  }

  it  should "call a nomatches handler function if it does not have any matches" in {
    var handlerCalled = false
    (
      onTestDB ~~> drop[Blog] ~~> findOne[Blog](titleField ?* ("blah"/)) { b => Some("Unexpected blog returned -> " + b) }
                { handlerCalled = true } ~~>()
    ) verifySuccess()

    handlerCalled should be (true)
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> findOne[Person](Person.nameField ?* ("*"/)){ _ => Some("Should not have return results") } { noOp } ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions thrown on creating a query" in {
    (
      onTestDB ~~> findOne[Blog](createExceptionalMongoObject) { _ => Some("Should not have return results") }
                { throw new RuntimeException("Handler should not be called on error") } ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exceptions thrown by match handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField("blah1"))) ~~>
              findOne[Blog](titleField ?* (".*"/)) { _ => throw new RuntimeException("Exception thrown handling match") }
                        { throw new RuntimeException("Handler should not be called on error") } ~~>()
    ) verifyError has ("Exception thrown handling match")
  }

  it should "handle exceptions thrown by nomatches handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              findOne[Blog](titleField ?* (".*"/)) { b => Some("Should not have return results -> " + b) }
                        { throw new RuntimeException("Handler function threw an Exception") } ~~>()
    ) verifyError has ("Handler function threw an Exception")
  }
}
