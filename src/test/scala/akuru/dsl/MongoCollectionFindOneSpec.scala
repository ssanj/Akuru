/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

final class MongoCollectionFindOneSpec extends CommonSpec with FindDSL with DSLTools {

  import Blog._

  "A MongoCollection with findOne" should "find a single match even if multiple matches exist" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(titleField === "blah1")) ~~> save(Blog(titleField === "blah2")) ~~> save(Blog(titleField === "blah3")) ~~>
            ( find * Blog where titleField === ("blah.*"/i) constrainedBy (Limit(1)) withResults { blogs =>
                blogs.size should equal (1)
                blogs(0).title.value should include regex ("blah")
                success
            } withoutResults Some("Should have found hits") )
    ) ~~>() verifySuccess
  }

  it  should "call a nomatches handler function if it does not have any matches" in {
    var handlerCalled = false
    (
      onTestDB ~~> drop[Blog] ~~> ( find * Blog where titleField === ("blah"/) withResults (b => Some("Unexpected blog returned -> " + b))
              withoutResults  { handlerCalled = true; None } ) ~~>()
    ) verifySuccess()

    handlerCalled should be (true)
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> ( find * Person where Person.nameField === ("*"/) withResults (_ => Some("Should not have return results"))
              withoutResults noOp ) ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions thrown on creating a query" in {
    (
      onTestDB ~~> ( find * Blog where (exceptionalFieldValueJoiner) withResults ( _ => Some("Should not have return results"))
              withoutResults  (Some("Handler should not be called on error")) ) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exceptions thrown by match handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField("blah1"))) ~~>
              ( find * Blog where titleField === (".*"/) withResults (_ => ex("Exception thrown handling match"))
                      withoutResults error("Handler should not be called on error") ) ~~>()
    ) verifyError has ("Exception thrown handling match")
  }

  it should "handle exceptions thrown by nomatches handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              ( find * Blog where titleField === (".*"/) withResults (b => Some("Should not have return results -> " + b))
                      withoutResults(Some("Handler function threw an Exception")) ) ~~>()
    ) verifyError has ("Handler function threw an Exception")
  }
}
