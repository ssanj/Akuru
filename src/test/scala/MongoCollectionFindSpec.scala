/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionFindSpec extends CommonSpec with FindOneDSL {

  import Blog._

  "A MongoCollection with find" should "find all saved objects matching a query" in {
    (onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField("sample1"), labelsField(Seq("sample")))) ~~>
              save(Blog(titleField("sample2"), labelsField(Seq("sample")))) ~~>
              save(Blog(titleField("sample3"), labelsField(Seq("sample")))) ~~>
              ( find many Blog where (titleField ?* ("sample*"/)) withResults { blogs =>
                blogs.size should equal (3)
                blogs.exists(_.title.value == "sample1") should be (true)
                blogs.exists(_.title.value == "sample2") should be (true)
                blogs.exists(_.title.value == "sample3") should be (true)
                success
              } )
    ) ~~>() verifySuccess
  }

  it should "return zero results if there are no matches" in {
    (onTestDB ~~>
              drop[Blog] ~~>
              ( find many (Blog) where (titleField ?* ("*"/)) withResults {blogs =>
                blogs.size should equal (0)
                success
              } )
    ) ~~>() verifySuccess
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~> (find many Person where (Person.nameField ?* ("*"/)) withResults (_ => fail("Should not have return results")) )~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions throw on creating a query" in {
    (
      onTestDB ~~> ( find many Blog where (createExceptionalMongoObject) withResults (_ => fail("Should not have return results")) ) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exception thrown by match handler function" in {
    (
      onTestDB ~~> ( find many Blog where (titleField ?* ("*"/)) withResults (_ => throw new RuntimeException("Handler threw an Exception")) ) ~~>()
    ) verifyError has ("Handler threw an Exception")
  }

  it should "find regex" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField("Querying with RegEx"), labelsField(Seq("query", "regex")))) ~~>
              ( find many Blog where (titleField ?* ("querying with RegEx"/)) withResults { blogs => blogs.size should equal (0); success } ) ~~>
              ( find many Blog where (titleField ?* ("Querying with RegEx"/i)) withResults { blogs => blogs.size should equal (1); success } ) ~~>
              ( find many Blog where (titleField ?* (".* with RegEx"/)) withResults { blogs => blogs.size should equal (1); success } ) ~~>
              ( find many Blog where (titleField ?* (".*query.*"/i)) withResults { blogs => blogs.size should equal (1); success } ) ~~>()
    ) verifySuccess
  }

  it should "sort results" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(titleField("Pears"), labelsField(Seq("fruit")))) ~~>
            save(Blog(titleField("Orange"), labelsField(Seq("fruit")))) ~~>
            save(Blog(titleField("Apple"), labelsField(Seq("fruit")))) ~~>
            save(Blog(titleField("WaterMellon"), labelsField(Seq("fruit")))) ~~>
            ( find many Blog where (titleField ?* (".*"/)) constrainedBy (Limit(2) and Order(titleField, ASC)) withResults {b =>
              b.size should equal (2)
              b(0).title.value should equal ("Apple")
              b(1).title.value should equal ("Orange")
              success
            } )
    ) ~~>()  verifySuccess
  }
}

