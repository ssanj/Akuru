/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionFindOneSpec extends CommonSpec with FindOneDSL {

  import Blog._

  "A MongoCollection with findOne" should "find a single match even if multiple matches exist" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(titleField("blah1"))) ~~> save(Blog(titleField("blah2"))) ~~> save(Blog(titleField("blah3"))) ~~>
            ( find one (Blog) where (titleField ?* ("blah.*"/i)) withResults { blog =>
                blog.title.value should include regex ("blah")
                success
            } onError { fail("Should have found hits") } )
    ) ~~>() verifySuccess
  }

  it  should "call a nomatches handler function if it does not have any matches" in {
    var handlerCalled = false
    (
      onTestDB ~~> drop[Blog] ~~> ( find one (Blog) where (titleField ?* ("blah"/)) withResults (b => Some("Unexpected blog returned -> " + b))
              onError (handlerCalled = true) ) ~~>()
    ) verifySuccess()

    handlerCalled should be (true)
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> ( find one (Person) where (Person.nameField ?* ("*"/)) withResults (_ => Some("Should not have return results"))
              onError(noOp) ) ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions thrown on creating a query" in {
    (
      onTestDB ~~> ( find one (Blog) where (createExceptionalMongoObject) withResults ( _ => Some("Should not have return results"))
              onError (throw new RuntimeException("Handler should not be called on error")) ) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exceptions thrown by match handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField("blah1"))) ~~>
              ( find one (Blog) where (titleField ?* (".*"/)) withResults (_ => throw new RuntimeException("Exception thrown handling match"))
                      onError(throw new RuntimeException("Handler should not be called on error")) ) ~~>()
    ) verifyError has ("Exception thrown handling match")
  }

  it should "handle exceptions thrown by nomatches handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              ( find one (Blog) where (titleField ?* (".*"/)) withResults (b => Some("Should not have return results -> " + b))
                      onError(throw new RuntimeException("Handler function threw an Exception")) ) ~~>()
    ) verifyError has ("Handler function threw an Exception")
  }
}
