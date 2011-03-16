/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait MongoCollectionCommonUpdateSpec { this:AkuruDSL with CommonSpec =>

  import MongoTypes.MongoObject.set
  import MongoTypes.MongoObject.push
  import Blog._

  def cardinality: UpdateQuery[Blog]
  def specName:String
  def expectedResults:Int

  specName should "handle Exceptions in the query" in {
    ( initBlog ~~>
            ( cardinality where (ex("boom!")) withValues (set(titleField === "Exceptional")) returnErrors )
    ) ~~>() verifyError(_ should startWith ("boom!"))
  }

  it should "handle Exceptions in the update" in {
    ( initBlog ~~>
            ( update many Blog where (titleField === "blah") withValues (ex("claboom!")) returnErrors )
    ) ~~>() verifyError(_ should startWith ("claboom!"))
  }

  it should "expectResults on matched updates" in {
    ( initBlog ~~>
        save(Blog(titleField === "Functor1", labelsField === Seq("fp"))) ~~>
        save(Blog(titleField === "Functor2", labelsField === Seq("fp"))) ~~>
        ( cardinality where titleField === ("Functor.*"/) withValues (push(labelsField, "tech"))
                expectResults (wr => if (wr.getN == Some(expectedResults)) None else
                  Some("Expected  Some(" + expectedResults + ") updates but got: " + wr.getN)) ) ~~>
        ( find (Blog) where labelsField === ("tech"/) withResults {b => b.size should equal (expectedResults); success}
                withoutResults error("Expected " + expectedResults + " but got 0") )
    ) ~~>() verifySuccess
  }

  it should "handle Exceptions on expectResults on matched updates" in {
    ( initBlog ~~>
        save(Blog(titleField === "Functor", labelsField === Seq("fp"))) ~~>
        ( cardinality where titleField === ("Functor"/) withValues (set(titleField === "^^functor^^"))
                expectResults (wr => ex("flattened functor")) )
    ) ~~>() verifyError(_ should startWith ("flattened functor"))
  }

  it should "not update multiple unmatched values" in {
    ( initBlog ~~>
            ( cardinality where (titleField === "Blog updates") withValues (set(titleField === "Phantom updates"))
                    expectResults {wr =>
                      runSafelyWithOptionReturnError {
                        wr.updatedExisting should equal (false)
                        wr.getN should equal (Some(0))
                        wr.ok should equal (true)
                    }}
            )
    ) ~~>() verifySuccess
  }

}