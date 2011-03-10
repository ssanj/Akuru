/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCollectionUpdateMultipleSpec extends AkuruDSL with CommonSpec {

  import Blog._
  import akuru.MongoTypes.MongoObject._

  "A MongoCollection with Update" should "not update multiple unmatched values" in {
    ( initBlog ~~>
            ( update many Blog where (titleField("Blog updates")) withValues (set(titleField("Phantom updates")))
                    expectResults {wr =>
                      runSafelyWithOptionReturnError {
                        wr.updatedExisting should equal (false)
                        wr.getN should equal (Some(0))
                        wr.ok should equal (true)
                    }}
            )
    ) ~~>() verifySuccess
  }

  it should "update multiple matched results" in {
    ( initBlog ~~>
      save(Blog(titleField("Functor"), labelsField(Seq("fp", "patterns")))) ~~>
      save(Blog(titleField("Applicative"), labelsField(Seq("fp", "patterns")))) ~~>
      save(Blog(titleField("Semigroup"), labelsField(Seq("fp", "patterns")))) ~~>
      save(Blog(titleField("Lessons Learned"), labelsField(Seq("work", "ideas")))) ~~>
      ( find many Blog where (labelsField ?* ("fp"/)) withResults {b => b.size should equal (3); success} ) ~~>
      ( update many Blog where (labelsField ?* ("fp"/)) withValues (set(labelsField(Seq("functional programming", "patterns")).splat()))
              returnErrors ) ~~>
      ( find many Blog where (labelsField ?* ("fp"/)) withResults {b => b.size should equal (0); success} ) ~~>
      ( find many Blog where (labelsField ?* ("functional programming"/)) withResults {b => b.size should equal (3); success} )
    ) ~~>() verifySuccess
  }

  it should "handle Exceptions in the query" in {
    ( initBlog ~~>
            ( update many Blog where (ex("boom!")) withValues (set(titleField("Exceptional"))) returnErrors )
    ) ~~>() verifyError(_ should startWith ("boom!"))
  }

  it should "handle Exceptions in the update" in {
    ( initBlog ~~>
            ( update many Blog where (titleField("blah")) withValues (ex("claboom!")) returnErrors )
    ) ~~>() verifyError(_ should startWith ("claboom!"))
  }

  it should "expectResults on matched updates" in {
    ( initBlog ~~>
        save(Blog(titleField("Functor1"), labelsField(Seq("fp")))) ~~>
        save(Blog(titleField("Functor2"), labelsField(Seq("fp")))) ~~>
        ( update many Blog where (titleField ?* ("Functor.*"/)) withValues (push(labelsField, "tech"))
                expectResults (wr => if (wr.getN == Some(2)) None else Some("Expected  2 updates but got: " + wr.getN)) ) ~~>
        ( find many Blog where (labelsField ?* ("tech"/)) withResults {b => b.size should equal (2); success} )
    ) ~~>() verifySuccess
  }
}